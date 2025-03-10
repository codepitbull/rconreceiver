package de.codepitbull.rcon.proto;

import java.util.Optional;

public interface Result {
    static Failure failure(String message, Throwable throwable){
        return new Failure(message, Optional.of(throwable));
    }

    static Failure failure(String message){
        return new Failure(message, Optional.empty());
    }

    static Success success(){
        return new Success();
    }

    record Success() implements Result{};
    record Failure(String message, Optional<Throwable> throwable) implements Result {};
}

