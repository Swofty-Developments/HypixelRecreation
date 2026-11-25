package io.github.term4.polyp.codegen;

import com.sun.source.util.Trees;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Emits {@code <Config>BuilderBase} for each {@link GenerateBuilder} config; see the annotation for the contract. */
@SupportedAnnotationTypes({"io.github.term4.polyp.codegen.GenerateBuilder", "io.github.term4.polyp.codegen.CheckResolveOrder"})
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public final class ConfigBuilderProcessor extends AbstractProcessor {

    private static final String FIELD_VALUE = "io.github.term4.polyp.config.FieldValue";

    private record Knob(String type, String name) {}

    /** AST scan for a {@code super(...)} / {@code copyKnobs} / {@code mergeKnobs} call - parsed identifiers, so comments can't satisfy it. */
    private static final class CopyCallScanner extends com.sun.source.util.TreeScanner<Boolean, Void> {
        @Override public Boolean visitMethodInvocation(com.sun.source.tree.MethodInvocationTree node, Void p) {
            String select = node.getMethodSelect().toString();
            if (select.equals("super") || select.endsWith("copyKnobs") || select.endsWith("mergeKnobs")
                    || select.endsWith("fromBase")) return true;
            return super.visitMethodInvocation(node, p);
        }
        @Override public Boolean reduce(Boolean a, Boolean b) { return Boolean.TRUE.equals(a) || Boolean.TRUE.equals(b); }
    }

    private Trees trees; // javac-only AST access for the copy-ctor check; null under other compilers

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        try {
            trees = Trees.instance(env);
        } catch (RuntimeException e) {
            trees = null;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment round) {
        for (Element e : round.getElementsAnnotatedWith(GenerateBuilder.class)) {
            if (e instanceof TypeElement config) {
                generate(config);
                checkCopyConstructors(config);
                checkFromBase(config);
            }
        }
        for (Element e : round.getElementsAnnotatedWith(CheckResolveOrder.class)) {
            if (e instanceof TypeElement resolver) checkResolveOrder(resolver);
        }
        return false;
    }

    /** A {@code fromBase} that forgets {@code mergeKnobs} silently drops every generated knob of the overlay. */
    private void checkFromBase(TypeElement config) {
        if (trees == null) return;
        for (Element member : config.getEnclosedElements()) {
            if (member.getKind() != ElementKind.METHOD || !member.getSimpleName().contentEquals("fromBase")) continue;
            var tree = trees.getTree((ExecutableElement) member);
            if (tree != null && tree.getBody() != null
                    && !Boolean.TRUE.equals(new CopyCallScanner().scan(tree.getBody(), null))) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "fromBase must merge the generated knobs (mergeKnobs, or super.fromBase)", member);
            }
        }
    }

    /** {@link CheckResolveOrder}: every {@code cfg.<knob>} argument must land on the same-named record component. */
    private void checkResolveOrder(TypeElement resolver) {
        if (trees == null) return;
        var path = trees.getPath(resolver);
        if (path == null) return;
        new com.sun.source.util.TreePathScanner<Void, Void>() {
            @Override public Void visitNewClass(com.sun.source.tree.NewClassTree node, Void p) {
                Element ctor = trees.getElement(new com.sun.source.util.TreePath(getCurrentPath(), node));
                if (ctor instanceof ExecutableElement ex
                        && ex.getEnclosingElement() instanceof TypeElement type
                        && type.getKind() == ElementKind.RECORD) {
                    var components = type.getRecordComponents();
                    var args = node.getArguments();
                    if (components.size() == args.size()) {
                        for (int i = 0; i < args.size(); i++) {
                            String read = firstComponentRead(args.get(i), type);
                            if (read != null && !components.get(i).getSimpleName().contentEquals(read)) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                        "argument " + (i + 1) + " of new " + type.getSimpleName() + " reads ." + read
                                                + " but the component there is " + components.get(i).getSimpleName(),
                                        resolver);
                            }
                        }
                    }
                }
                return super.visitNewClass(node, p);
            }

            /** The first {@code x.<name>} select in {@code arg} whose name matches a component of {@code type}. */
            private String firstComponentRead(com.sun.source.tree.Tree arg, TypeElement type) {
                var found = new String[1];
                new com.sun.source.util.TreeScanner<Void, Void>() {
                    @Override public Void visitMemberSelect(com.sun.source.tree.MemberSelectTree sel, Void p) {
                        if (found[0] == null) {
                            String name = sel.getIdentifier().toString();
                            for (var c : type.getRecordComponents()) {
                                if (c.getSimpleName().contentEquals(name)) { found[0] = name; break; }
                            }
                        }
                        return super.visitMemberSelect(sel, p);
                    }
                }.scan(arg, null);
                return found[0];
            }
        }.scan(path, null);
    }

    /**
     * A hand-written {@code Builder(Config c)} that forgets {@code super(c)} silently resets every generated
     * knob to defaults (this shipped: fireballFight() lost knockbackMultiplier through toBuilder()).
     */
    private void checkCopyConstructors(TypeElement config) {
        if (trees == null) return;
        for (Element member : config.getEnclosedElements()) {
            if (member.getKind() != ElementKind.CLASS || !member.getSimpleName().contentEquals("Builder")) continue;
            for (Element ctor : member.getEnclosedElements()) {
                if (ctor.getKind() != ElementKind.CONSTRUCTOR) continue;
                ExecutableElement ex = (ExecutableElement) ctor;
                boolean takesConfig = ex.getParameters().stream()
                        .anyMatch(p -> processingEnv.getTypeUtils().isSameType(p.asType(), config.asType()));
                if (!takesConfig) continue;
                var tree = trees.getTree(ex);
                if (tree != null && tree.getBody() != null
                        && !Boolean.TRUE.equals(new CopyCallScanner().scan(tree.getBody(), null))) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Builder copy-constructor must call super(c) - the generated knobs reset to defaults otherwise", ctor);
                }
            }
        }
    }

    private void generate(TypeElement config) {
        String pkg = processingEnv.getElementUtils().getPackageOf(config).getQualifiedName().toString();
        String cfg = config.getSimpleName().toString();
        String base = cfg + "BuilderBase";

        String ctx = null;
        List<Knob> knobs = new ArrayList<>();
        for (Element member : config.getEnclosedElements()) {
            if (member.getKind() != ElementKind.FIELD) continue;
            if (!(member.asType() instanceof DeclaredType dt)) continue;
            if (!((TypeElement) dt.asElement()).getQualifiedName().contentEquals(FIELD_VALUE)) continue;
            List<? extends TypeMirror> args = dt.getTypeArguments();
            if (args.size() != 2) continue;
            ctx = args.get(0).toString();
            knobs.add(new Knob(args.get(1).toString(), member.getSimpleName().toString()));
        }
        if (ctx == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "no FieldValue fields to generate from", config);
            return;
        }

        StringBuilder s = new StringBuilder();
        s.append("package ").append(pkg).append(";\n\n")
         .append("import io.github.term4.polyp.config.FieldValue;\n\n")
         .append("import java.util.function.Function;\n\n")
         .append("/** Generated from {@link ").append(cfg).append("}'s FieldValue fields (").append(GenerateBuilder.class.getSimpleName()).append(") - do not edit. */\n")
         .append("abstract class ").append(base).append("<B extends ").append(base).append("<B>> {\n\n");
        for (Knob k : knobs) {
            s.append("    FieldValue<").append(ctx).append(", ").append(k.type).append("> ").append(k.name).append(";\n");
        }
        s.append("\n    protected abstract B self();\n\n")
         .append("    protected ").append(base).append("() {}\n\n")
         .append("    /** The copy route for hand-written {@code Builder(Config c)} ctors - enforced by the processor. */\n")
         .append("    protected ").append(base).append("(").append(cfg).append(" c) { copyKnobs(c); }\n\n");
        for (Knob k : knobs) {
            String t = k.type, n = k.name;
            s.append("    public B ").append(n).append("(").append(t).append(" v) { ").append(n).append(" = FieldValue.constant(v); return self(); }\n")
             .append("    public B ").append(n).append("(Function<").append(ctx).append(", ").append(t).append("> fn) { ").append(n).append(" = FieldValue.of(fn); return self(); }\n")
             .append("    public B ").append(n).append("(").append(t).append(" fallback, Function<").append(ctx).append(", ").append(t).append("> fn) { ").append(n).append(" = FieldValue.ofWithFallback(fallback, fn); return self(); }\n")
             .append("    B ").append(n).append("(FieldValue<").append(ctx).append(", ").append(t).append("> v) { ").append(n).append(" = v; return self(); }\n");
        }
        s.append("\n    /** Copies every generated knob from {@code c}. */\n    final void copyKnobs(").append(cfg).append(" c) {\n");
        for (Knob k : knobs) s.append("        ").append(k.name).append(" = c.").append(k.name).append(";\n");
        s.append("    }\n");
        s.append("\n    /** Sets every generated knob to {@code a} layered over {@code base} ({@code FieldValue.merge}). */\n    final void mergeKnobs(").append(cfg).append(" a, ").append(cfg).append(" base) {\n");
        for (Knob k : knobs) s.append("        ").append(k.name).append(" = FieldValue.merge(a.").append(k.name).append(", base.").append(k.name).append(");\n");
        s.append("    }\n}\n");

        try {
            Writer w = processingEnv.getFiler().createSourceFile(pkg + "." + base, config).openWriter();
            try (w) { w.write(s.toString()); }
        } catch (IOException ex) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "codegen failed: " + ex, config);
        }
    }
}
