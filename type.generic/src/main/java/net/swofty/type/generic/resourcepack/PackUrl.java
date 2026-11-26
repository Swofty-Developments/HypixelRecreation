package net.swofty.type.generic.resourcepack;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

public final class PackUrl {
    private static final Set<String> LOOPBACK_HOSTS = Set.of("127.0.0.1", "localhost", "0.0.0.0", "::1", "[::1]");

    private PackUrl() {
    }

    public static URI forConnection(String packUrl, String connectedHost) {
        URI uri = URI.create(packUrl);
        String host = cleanHost(connectedHost);
        if (host == null || uri.getHost() == null || !LOOPBACK_HOSTS.contains(uri.getHost().toLowerCase(Locale.ROOT))) {
            return uri;
        }
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), host, uri.getPort(),
                    uri.getPath(), uri.getQuery(), uri.getFragment());
        } catch (URISyntaxException exception) {
            return uri;
        }
    }

    private static String cleanHost(String connectedHost) {
        if (connectedHost == null) {
            return null;
        }
        String host = connectedHost;
        int marker = host.indexOf('\0');
        if (marker >= 0) {
            host = host.substring(0, marker);
        }
        host = host.strip();
        while (host.endsWith(".")) {
            host = host.substring(0, host.length() - 1);
        }
        if (host.startsWith("[") && host.endsWith("]")) {
            host = host.substring(1, host.length() - 1);
        }
        return host.isEmpty() ? null : host;
    }
}
