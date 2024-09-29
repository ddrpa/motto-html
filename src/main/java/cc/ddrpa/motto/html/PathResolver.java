package cc.ddrpa.motto.html;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathResolver {

    public static final int UNKNOWN = 0;
    public static final int LINUX = 1;
    public static final int WINDOWS = 2;
    public static final int MAC_OS = 3;

    private static final int os = whichOS();

    public static List<Path> preinstalledFontPaths() {
        List<Path> fontPaths = new ArrayList<>(4);
        String userHomeDir = System.getProperty("user.home");
        switch (os) {
            case LINUX:
                fontPaths.add(Path.of("/usr/share/fonts"));
                fontPaths.add(Path.of("/usr/local/share/fonts"));
                fontPaths.add(Path.of(userHomeDir, ".fonts"));
                fontPaths.add(Path.of(userHomeDir, ".local", "share", "fonts"));
                break;
            case MAC_OS:
                fontPaths.add(Path.of(userHomeDir, "Library", "Fonts"));
                fontPaths.add(Path.of("/Library/Fonts"));
                fontPaths.add(Path.of("/System/Library/Fonts"));
                break;
            case WINDOWS:
                fontPaths.add(Path.of("C:", "Windows", "Fonts"));
                break;
            default:
                break;
        }
        // NEED_CHECK 未判断是文件还是目录
        return fontPaths.stream().filter(path -> path.toFile().exists()).toList();
    }

    private static int whichOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("nix") || osName.contains("nux")) {
            return LINUX;
        } else if (osName.contains("windows")) {
            return WINDOWS;
        } else if (osName.contains("mac")) {
            return MAC_OS;
        } else {
            return UNKNOWN;
        }
    }
}
