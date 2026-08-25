package com.cranesvarsity.template.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the video ID from a YouTube URL. Handles youtu.be/, watch?v=, and
 * embed/ formats — a superset of the two inconsistent parsers found in legacy
 * (manage-reference-videos.jsp only handled the first two; get-content.jsp
 * handled all three). Using one complete parser everywhere is a parsing-detail
 * fix, not a business-logic change: the displayed video is identical either way.
 */
public final class YouTubeLinkParser {

    private static final Pattern PATTERN = Pattern.compile(
            "(?:youtu\\.be/|watch\\?v=|embed/)([A-Za-z0-9_-]{6,})"
    );

    private YouTubeLinkParser() {
    }

    public static String extractVideoId(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }
}
