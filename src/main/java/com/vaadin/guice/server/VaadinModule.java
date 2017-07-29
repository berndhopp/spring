package com.vaadin.guice.server;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

import com.vaadin.guice.annotation.GuiceUI;
import com.vaadin.guice.annotation.GuiceView;
import com.vaadin.guice.annotation.UIScope;
import com.vaadin.guice.annotation.VaadinSessionScope;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;

import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

class VaadinModule extends AbstractModule {

    private final GuiceVaadinServlet guiceVaadinServlet;

    VaadinModule(GuiceVaadinServlet GuiceVaadinServlet) {
        this.guiceVaadinServlet = GuiceVaadinServlet;
    }

    @Override
    protected void configure() {
        bindScope(UIScope.class, guiceVaadinServlet.getUiScoper());
        bindScope(GuiceView.class, guiceVaadinServlet.getUiScoper());
        bindScope(GuiceUI.class, guiceVaadinServlet.getVaadinSessionScoper());
        bindScope(VaadinSessionScope.class, guiceVaadinServlet.getVaadinSessionScoper());
        bind(GuiceUIProvider.class).toInstance(guiceVaadinServlet.getGuiceUIProvider());
        bind(ViewProvider.class).toInstance(guiceVaadinServlet.getViewProvider());
        bind(VaadinSession.class).toProvider(guiceVaadinServlet.getVaadinSessionProvider());
        bind(UI.class).toProvider(guiceVaadinServlet.getCurrentUIProvider());
        bind(VaadinService.class).toProvider(guiceVaadinServlet.getVaadinServiceProvider());

        guiceVaadinServlet.getUis().forEach(this::bindUI);

        bind(new TypeLiteral<Set<Class<? extends View>>>(){}).toProvider(new NavigableViewsProvider(guiceVaadinServlet));

    }

    private <T extends UI> void bindUI(Class<T> uiClass){

        try {
            checkArgument(uiClass.getConstructors().length == 1);

            uiClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        bind(uiClass).toProvider(new UIProvider<>(guiceVaadinServlet, uiClass));
    }
}
