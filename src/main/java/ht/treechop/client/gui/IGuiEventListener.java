package ht.treechop.client.gui;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public interface IGuiEventListener {

    default List<? extends IGuiEventListener> getEventListeners() {
        return Collections.emptyList();
    }

    boolean isMouseOver(int mouseX, int mouseY);

    default void onClick(int mouseX, int mouseY, int button) {
        Optional<IGuiEventListener> clickedListener = getEventListenerForPos(mouseX, mouseY);
        clickedListener.ifPresent(listener -> listener.onClick(mouseX, mouseY, button));
    }

    default Optional<IGuiEventListener> getEventListenerForPos(int mouseX, int mouseY) {
        for(IGuiEventListener listener : this.getEventListeners()) {
            if (listener.isMouseOver(mouseX, mouseY)) {
                return Optional.of(listener);
            }
        }

        return Optional.empty();
    }

}
