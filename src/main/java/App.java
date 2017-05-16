import java.time.Instant;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import org.apache.logging.log4j.util.StringBuilderFormattable;

public final class App {
    private static final Logger log = LogManager.getLogger();

    public static void main(String[] args) {
        // XXX could also use Boon or Jackson JSON libs
        Gson gson = new Gson();

        // Secenario 1: root logger, lazily log via Java 8 lambdas
        // XXX local vars called from lambda must be final or effectively final
        final Map<String,String> evt = new LinkedHashMap<>();
        evt.put("timestamp", Instant.now().toString());
        evt.put("library", "log4j2-lambda");
        evt.put("click", "clack");
        evt.put("woof", "grrr");
        log.info("{}", () -> gson.toJson(evt));

        // Scenario 2: root logger, custom EventMessage
        Map<String,String> event = new LinkedHashMap<>();
        event.put("timestamp", Instant.now().toString());
        event.put("library", "log4j2");
        event.put("foo", "bar");
        event.put("que", "baz");
        log.info(new JsonEventMessage(gson, event));

        // Scenario 3: root logger, SLF4J API not log4j2 API
        // TODO refactor all plugins from SLF4J API to log4j2 API?
        org.slf4j.Logger slf4j = org.slf4j.LoggerFactory.getLogger(App.class);
        event = new LinkedHashMap<>();
        event.put("timestamp", Instant.now().toString());
        event.put("library", "slf4j");
        event.put("soo", "me");
        event.put("ha", "blah");
        slf4j.info("{}", gson.toJson(event));

        // Scenario 4: root logger with Marker, MarkerFilter, custom EventMessage,
        //             separate filtering Appender
        Marker EVENTARC_MARKER = MarkerManager.getMarker("EVENTARC");
        final Map<String,String> evt1 = new LinkedHashMap<>();
        evt1.put("timestamp", Instant.now().toString());
        evt1.put("library", "log4j-marker");
        evt1.put("wolf", "paw");
        evt1.put("moo", "lu");
        log.info(EVENTARC_MARKER, () -> gson.toJson(evt1));

        // Scenario 5: EventARC dedicated logger, lambda, separate Appender
        // TODO implement

        // Scenario 6: root logger, custom EventMessage, JDBC Appender
        //             H2+HikariCP -OR- stimpy's MySQL+HikariCP into blob/JSON column
        // TODO implement, confirm also handles lambdas

        // Scenario 7: root logger, custom EventMessage, SMTP Appender and simple email body
        // TODO implement, confirm also handles lambdas
    }


    // Custom Message implementation converting a Map<String,String> to a JSON string
    // when asked by log4j2 API. Not really needed since we can simply write
    //
    //      log.info("{}", () -> gson.toJson(map))
    //
    // in which log4j2 lazily logs by only calling gson.toJson(map) if logging is enabled
    // at the specified level.
    private static final class JsonEventMessage implements StringBuilderFormattable {

        private final Gson _gson;
        private final Map<String,String> _event;

        public JsonEventMessage(final Gson gson, final Map<String,String> event) {
            _event = event;
            _gson = gson;
        }

        @Override
        public void formatTo(final StringBuilder buf) {
            _gson.toJson(_event, buf);
        }
    }
}

// vim:et
