package io.safecorners.springbootreactive.actuator;

import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;

import java.util.List;
import java.util.stream.Collectors;

public class SpringDataHttpTraceRepository implements HttpTraceRepository {

    private final HttpTraceWrapperRepository httpTraceWrapperRepository;

    public SpringDataHttpTraceRepository(HttpTraceWrapperRepository httpTraceWrapperRepository) {
        this.httpTraceWrapperRepository = httpTraceWrapperRepository;
    }

    @Override
    public List<HttpTrace> findAll() {
        return httpTraceWrapperRepository.findAll()
                .map(HttpTraceWrapper::getHttpTrace)
                .collect(Collectors.toList());
    }

    @Override
    public void add(HttpTrace trace) {
        httpTraceWrapperRepository.save(new HttpTraceWrapper(trace));
    }
}
