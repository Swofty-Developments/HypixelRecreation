package net.swofty.type.skyblockgeneric.item.crafting;

import net.swofty.type.skyblockgeneric.string.PlayerTemplateProcessor;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class CraftingRequirement {
    private enum Kind {
        EXPRESSION,
        ALL,
        ANY,
        NOT
    }

    private final String id;
    private final Kind kind;
    private final String leftExpression;
    private final Operator operator;
    private final String rightExpression;
    private final String failureMessage;
    private final List<CraftingRequirement> children;

    private CraftingRequirement(String id,
                                Kind kind,
                                String leftExpression,
                                Operator operator,
                                String rightExpression,
                                String failureMessage,
                                List<CraftingRequirement> children) {
        this.id = id;
        this.kind = kind;
        this.leftExpression = leftExpression;
        this.operator = operator;
        this.rightExpression = rightExpression;
        this.failureMessage = failureMessage;
        this.children = children == null ? List.of() : List.copyOf(children);
    }

    public static CraftingRequirement expression(String id,
                                                 String leftExpression,
                                                 Operator operator,
                                                 String rightExpression,
                                                 String failureMessage) {
        if (leftExpression == null || leftExpression.isBlank()) {
            throw new IllegalArgumentException("Crafting requirement left expression cannot be blank");
        }
        if (rightExpression == null) {
            throw new IllegalArgumentException("Crafting requirement right expression cannot be null");
        }

        return new CraftingRequirement(
                normalizeId(id),
                Kind.EXPRESSION,
                leftExpression,
                Objects.requireNonNull(operator, "operator"),
                rightExpression,
                normalizeFailureMessage(failureMessage, id),
                List.of()
        );
    }

    public static CraftingRequirement all(String id, List<CraftingRequirement> children) {
        return composite(id, Kind.ALL, children);
    }

    public static CraftingRequirement any(String id, List<CraftingRequirement> children) {
        return composite(id, Kind.ANY, children);
    }

    public static CraftingRequirement not(String id, CraftingRequirement child, String failureMessage) {
        return new CraftingRequirement(
                normalizeId(id),
                Kind.NOT,
                null,
                null,
                null,
                normalizeFailureMessage(failureMessage, id),
                List.of(Objects.requireNonNull(child, "child"))
        );
    }

    private static CraftingRequirement composite(String id, Kind kind, List<CraftingRequirement> children) {
        List<CraftingRequirement> safeChildren = children == null ? List.of() : children;
        return new CraftingRequirement(
                normalizeId(id),
                kind,
                null,
                null,
                null,
                "<c>Requirement not met: " + normalizeId(id),
                safeChildren
        );
    }

    public Result evaluate(SkyBlockPlayer player) {
        return switch (kind) {
            case EXPRESSION -> evaluateExpression(player);
            case ALL -> evaluateAll(player);
            case ANY -> evaluateAny(player);
            case NOT -> evaluateNot(player);
        };
    }

    public String id() {
        return id;
    }

    public List<CraftingRequirement> children() {
        return children;
    }

    private Result evaluateExpression(SkyBlockPlayer player) {
        try {
            PlayerTemplateProcessor processor = new PlayerTemplateProcessor(player);
            String left = processor.parseMessage(leftExpression).trim();
            String right = processor.parseMessage(rightExpression).trim();
            return operator.matches(left, right)
                    ? Result.passed()
                    : Result.denied(failureMessage);
        } catch (RuntimeException ignored) {
            return Result.denied(failureMessage);
        }
    }

    private Result evaluateAll(SkyBlockPlayer player) {
        List<String> failures = new ArrayList<>();
        for (CraftingRequirement child : children) {
            Result result = child.evaluate(player);
            if (!result.allowed()) failures.addAll(result.failureMessages());
        }
        return failures.isEmpty() ? Result.passed() : Result.denied(failures);
    }

    private Result evaluateAny(SkyBlockPlayer player) {
        List<String> failures = new ArrayList<>();
        for (CraftingRequirement child : children) {
            Result result = child.evaluate(player);
            if (result.allowed()) return Result.passed();
            failures.addAll(result.failureMessages());
        }
        return failures.isEmpty() ? Result.denied(failureMessage) : Result.denied(failures);
    }

    private Result evaluateNot(SkyBlockPlayer player) {
        Result result = children.getFirst().evaluate(player);
        return result.allowed() ? Result.denied(failureMessage) : Result.passed();
    }

    private static String normalizeId(String id) {
        return id == null || id.isBlank() ? "requirement" : id;
    }

    private static String normalizeFailureMessage(String message, String id) {
        return message == null || message.isBlank()
                ? "<c>Requirement not met: " + normalizeId(id)
                : message;
    }

    public enum Operator {
        GREATER_THAN_OR_EQUAL,
        GREATER_THAN,
        EQUAL,
        LESS_THAN,
        LESS_THAN_OR_EQUAL,
        NOT_EQUAL,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        MATCHES;

        public static Operator parse(String value) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Crafting requirement operation cannot be blank");
            }

            return switch (value.trim().toLowerCase()) {
                case ">=", "gte", "ge" -> GREATER_THAN_OR_EQUAL;
                case ">", "gt" -> GREATER_THAN;
                case "=", "==", "eq", "equals" -> EQUAL;
                case "<", "lt" -> LESS_THAN;
                case "<=", "lte", "le" -> LESS_THAN_OR_EQUAL;
                case "!=", "ne", "not_equals" -> NOT_EQUAL;
                case "contains", "includes" -> CONTAINS;
                case "startswith", "starts_with" -> STARTS_WITH;
                case "endswith", "ends_with" -> ENDS_WITH;
                case "matches", "regex" -> MATCHES;
                default -> throw new IllegalArgumentException("Invalid crafting requirement operation: " + value);
            };
        }

        private boolean matches(String left, String right) {
            Double leftNumber = number(left);
            Double rightNumber = number(right);

            if (leftNumber != null && rightNumber != null) {
                return switch (this) {
                    case GREATER_THAN_OR_EQUAL -> leftNumber >= rightNumber;
                    case GREATER_THAN -> leftNumber > rightNumber;
                    case EQUAL -> Double.compare(leftNumber, rightNumber) == 0;
                    case LESS_THAN -> leftNumber < rightNumber;
                    case LESS_THAN_OR_EQUAL -> leftNumber <= rightNumber;
                    case NOT_EQUAL -> Double.compare(leftNumber, rightNumber) != 0;
                    case CONTAINS, STARTS_WITH, ENDS_WITH, MATCHES -> stringMatches(left, right);
                };
            }

            return switch (this) {
                case EQUAL -> left.equals(right);
                case NOT_EQUAL -> !left.equals(right);
                case CONTAINS -> left.contains(right);
                case STARTS_WITH -> left.startsWith(right);
                case ENDS_WITH -> left.endsWith(right);
                case MATCHES -> Pattern.matches(right, left);
                case GREATER_THAN_OR_EQUAL, GREATER_THAN, LESS_THAN, LESS_THAN_OR_EQUAL -> false;
            };
        }

        private boolean stringMatches(String left, String right) {
            return switch (this) {
                case CONTAINS -> left.contains(right);
                case STARTS_WITH -> left.startsWith(right);
                case ENDS_WITH -> left.endsWith(right);
                case MATCHES -> Pattern.matches(right, left);
                default -> false;
            };
        }

        private static Double number(String value) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    public record Result(boolean allowed, List<String> failureMessages) {
        public Result {
            failureMessages = failureMessages == null ? List.of() : List.copyOf(failureMessages);
        }

        public static Result passed() {
            return new Result(true, List.of());
        }

        public static Result denied(String failureMessage) {
            return new Result(false, failureMessage == null ? List.of() : List.of(failureMessage));
        }

        public static Result denied(List<String> failureMessages) {
            return new Result(false, failureMessages);
        }
    }
}
