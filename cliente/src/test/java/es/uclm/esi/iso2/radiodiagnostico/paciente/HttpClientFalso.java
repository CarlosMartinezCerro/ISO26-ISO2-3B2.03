package es.uclm.esi.iso2.radiodiagnostico.paciente;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

/** {@link HttpClient} sin red: guarda las peticiones y responde o falla como se le indique. */
final class HttpClientFalso extends HttpClient {

    final List<HttpRequest> peticiones = new ArrayList<>();
    final List<String> cuerpos = new ArrayList<>();
    int codigo = 200;
    String cuerpo = "";
    IOException falloRed;
    InterruptedException interrupcion;

    @Override
    @SuppressWarnings("unchecked") // El cliente que se prueba siempre pide el cuerpo como String.
    public <T> HttpResponse<T> send(HttpRequest peticion, HttpResponse.BodyHandler<T> manejador)
            throws IOException, InterruptedException {
        peticiones.add(peticion);
        cuerpos.add(leerCuerpo(peticion));
        if (falloRed != null) {
            throw falloRed;
        }
        if (interrupcion != null) {
            throw interrupcion;
        }
        return (HttpResponse<T>) new Respuesta(codigo, cuerpo, peticion);
    }

    private static String leerCuerpo(HttpRequest peticion) {
        StringBuilder texto = new StringBuilder();
        peticion.bodyPublisher().orElseThrow().subscribe(new Flow.Subscriber<ByteBuffer>() {
            @Override
            public void onSubscribe(Flow.Subscription suscripcion) {
                suscripcion.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ByteBuffer bytes) {
                texto.append(StandardCharsets.UTF_8.decode(bytes));
            }

            @Override
            public void onError(Throwable error) {
                throw new IllegalStateException(error);
            }

            @Override
            public void onComplete() {
                // No hay nada que liberar.
            }
        });
        return texto.toString();
    }

    private record Respuesta(int statusCode, String body, HttpRequest request)
            implements HttpResponse<String> {

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (nombre, valor) -> true);
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public HttpClient.Version version() {
            return HttpClient.Version.HTTP_1_1;
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest peticion,
            HttpResponse.BodyHandler<T> manejador) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest peticion,
            HttpResponse.BodyHandler<T> manejador, HttpResponse.PushPromiseHandler<T> promesas) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return Redirect.NEVER;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SSLParameters sslParameters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return Version.HTTP_1_1;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }
}
