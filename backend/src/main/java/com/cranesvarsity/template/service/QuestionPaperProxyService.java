package com.cranesvarsity.template.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Successor of the legacy ProxyPDFServlet.
 *
 * The question paper lives in S3 behind a public-read URL. If the browser were
 * given that URL it would be shareable forever, by anyone, with no test window
 * and no login in front of it — so the URL never leaves the server. This
 * fetches the file server-side and the controller streams the bytes back from
 * our own origin, which also sidesteps CORS and keeps the PDF viewer on a
 * same-origin blob.
 *
 * This is not DRM. A student can still photograph the screen. It exists to stop
 * the casual save/share, and to make the file useless once the test window has
 * closed.
 */
@Service
public class QuestionPaperProxyService {

    private static final Logger log = LoggerFactory.getLogger(QuestionPaperProxyService.class);

    /** Question papers are a handful of pages; anything past this is not one. */
    private static final int MAX_BYTES = 25 * 1024 * 1024;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Fetch the paper from storage.
     *
     * @param fileUrl the storage URL from manage_theory_questions_pdf — from
     *                our own database, never from the request
     */
    public byte[] fetch(String fileUrl) {
        URI uri = parseHttpUri(fileUrl);
        try {
            HttpResponse<byte[]> response = httpClient.send(
                    HttpRequest.newBuilder(uri)
                            .timeout(Duration.ofSeconds(30))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                log.warn("Question paper fetch for {} returned HTTP {}", uri, response.statusCode());
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The question paper could not be retrieved. Please inform your trainer.");
            }

            byte[] body = response.body();
            if (body == null || body.length == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The question paper file is empty. Please inform your trainer.");
            }
            if (body.length > MAX_BYTES) {
                log.warn("Question paper at {} is {} bytes, over the {} byte cap", uri, body.length, MAX_BYTES);
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "The question paper is too large to display.");
            }
            return body;

        } catch (IOException e) {
            log.warn("Question paper fetch for {} failed: {}", uri, e.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The question paper could not be retrieved. Please try again.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The question paper fetch was interrupted. Please try again.");
        }
    }

    /**
     * Only http(s) is proxied.
     *
     * The URL comes from a trainer-populated column rather than from the
     * request, but a typo (or a pasted file:// path) should fail here rather
     * than turn this into a reader of the server's own filesystem.
     *
     * The links are also not pre-encoded — module names go into the S3 key
     * verbatim, so a real row reads ".../theory-pdfs/System Verilog/...". A raw
     * space is illegal in a URI, so the path is re-encoded here rather than
     * rejected: this is what the stored data actually looks like.
     */
    private URI parseHttpUri(String fileUrl) {
        try {
            URL url = new URL(fileUrl.trim());
            String scheme = url.getProtocol();
            if (!(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("scheme " + scheme);
            }
            // This URI constructor quotes whatever is illegal in each component,
            // and leaves anything already percent-encoded alone.
            return new URI(scheme, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef());
        } catch (Exception e) {
            log.warn("Question paper link is not a usable http(s) URL: '{}'", fileUrl);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "The question paper link is not valid. Please inform your trainer.");
        }
    }
}
