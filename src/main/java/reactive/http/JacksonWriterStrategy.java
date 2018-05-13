/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package reactive.http;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.client.HttpClientRequest;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Write to a request from an {@code Object} to a JSON {@code String} using Jackson 2.9.
 */
public class JacksonWriterStrategy implements WriterStrategy<Object> {

    private final ObjectMapper objectMapper;

    public JacksonWriterStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canWrite(@Nullable Class<?> type, @Nullable String contentType) {
        if (type == null || contentType == null || !contentType.startsWith("application/json")) {
            return false;
        }
        Class<?> rawClass = getJavaType(type).getRawClass();

        return (Object.class == rawClass)
                || !String.class.isAssignableFrom(rawClass) && objectMapper.canSerialize(rawClass);
    }

    @Override
    public Mono<Void> write(HttpClientRequest request, @Nullable Object body) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(body);
        try {
            return request.sendString(Mono.just(objectMapper.writeValueAsString(body))).then();
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    private JavaType getJavaType(Type type) {
        TypeFactory typeFactory = this.objectMapper.getTypeFactory();
        return typeFactory.constructType(type);
    }
}
