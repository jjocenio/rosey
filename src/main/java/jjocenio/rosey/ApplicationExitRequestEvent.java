package jjocenio.rosey.service;

import org.springframework.context.ApplicationEvent;

public class ApplicationExitRequestEvent extends ApplicationEvent {

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public ApplicationExitRequestEvent(Object source) {
        super(source);
    }
}
