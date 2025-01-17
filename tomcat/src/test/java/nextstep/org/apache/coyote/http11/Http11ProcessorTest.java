package nextstep.org.apache.coyote.http11;

import static org.apache.coyote.Constants.CRLF;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import org.apache.catalina.SessionManager;
import org.apache.coyote.http11.Http11Processor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.StubSocket;

class Http11ProcessorTest {

    @BeforeEach
    void setUp() {
        SessionManager.get().removeAll();
    }

    @Test
    @DisplayName("웰컴 페이지를 응답한다.")
    void process() {
        // given
        final String httpRequest = String.join(CRLF,
                "GET / HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final var processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        String expected = String.join(CRLF,
                "HTTP/1.1 200 OK ",
                "Content-Type: text/html;charset=utf-8 ",
                "Content-Length: 12 ",
                "",
                "Hello world!");

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("메인 페이지를 응답한다.")
    void index() {
        // given
        final String httpRequest = String.join(CRLF,
                "GET /index.html HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/index.html");
        var expected = "HTTP/1.1 200 OK " + CRLF +
                "Content-Type: text/html;charset=utf-8 " + CRLF +
                "Content-Length: 5564 " + CRLF +
                CRLF +
                getBody(resource);

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("CSS 파일을 응답한다.")
    void css() {
        // given
        final String httpRequest = String.join(CRLF,
                "GET /css/styles.css HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "",
                "");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/css/styles.css");
        var expected = "HTTP/1.1 200 OK " + CRLF +
                "Content-Type: text/css;charset=utf-8 " + CRLF +
                "Content-Length: 211991 " + CRLF +
                CRLF +
                getBody(resource);

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("로그인 성공 시 메인 페이지를 응답한다.")
    void login() {
        // given
        final String httpRequest = String.join(CRLF,
                "POST /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 30",
                "",
                "account=gugu&password=password");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        var expected = "HTTP/1.1 302 Found " + CRLF +
                "Location: /index.html " + CRLF +
                "Set-Cookie: JSESSIONID=";

        assertThat(socket.output()).contains(expected);
        int size = SessionManager.get().size();
        System.out.println("cookie = " + size);
        assertThat(size).isEqualTo(1);
    }

    @Test
    @DisplayName("로그인 실패 시 401 페이지를 응답한다.")
    void login_fail() {
        // given
        final String httpRequest = String.join(CRLF,
                "POST /login HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 27",
                "",
                "account=gugu2&password=nono");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/401.html");
        var expected = "HTTP/1.1 401 Unauthorized " + CRLF +
                "Content-Type: text/html;charset=utf-8 " + CRLF +
                "Content-Length: 2426 " + CRLF +
                CRLF +
                getBody(resource);

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원가입 성공 시 메인 페이지를 응답한다.")
    void register() {
        // given
        final String httpRequest = String.join(CRLF,
                "POST /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 80 ",
                "Content-Type: application/x-www-form-urlencoded ",
                "Accept: */* ",
                "",
                "account=gugu2&password=password&email=hkkang%40woowahan.com");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        var expected = "HTTP/1.1 302 Found " + CRLF +
                "Location: /index.html " + CRLF +
                CRLF +
                "";

        assertThat(socket.output()).isEqualTo(expected);
    }

    @Test
    @DisplayName("회원가입 실패 시 500 페이지를 응답한다.")
    void register_fail() {
        // given
        final String httpRequest = String.join(CRLF,
                "POST /register HTTP/1.1 ",
                "Host: localhost:8080 ",
                "Connection: keep-alive ",
                "Content-Length: 80 ",
                "Content-Type: application/x-www-form-urlencoded ",
                "Accept: */* ",
                "",
                "account=gugu&password=password&email=hkkang%40woowahan.com");

        final var socket = new StubSocket(httpRequest);
        final Http11Processor processor = new Http11Processor(socket);

        // when
        processor.process(socket);

        // then
        final URL resource = getClass().getClassLoader().getResource("static/500.html");
        var expected = "HTTP/1.1 500 Internal Server Error " + CRLF +
                "Content-Type: text/html;charset=utf-8 " + CRLF +
                "Content-Length: 2357 " + CRLF +
                CRLF +
                getBody(resource);

        assertThat(socket.output()).isEqualTo(expected);
    }

    private String getBody(URL resource) {
        try {
            if (resource == null) {
                throw new RuntimeException();
            }
            return new String(Files.readAllBytes(new File(resource.getFile()).toPath()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
