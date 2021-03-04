package com.uncles.novel.app.jfx.framework.app;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.stage.StageHelper;
import com.uncles.novel.app.jfx.framework.lifecycle.LifeCycle;
import com.uncles.novel.app.jfx.framework.ui.components.decorator.StageDecorator;
import com.uncles.novel.app.jfx.framework.util.FxmlLoader;
import com.uncles.novel.app.jfx.framework.util.ResourceUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义 Application
 *
 * @author blog.unclezs.com
 * @since 2021/02/26 10:50
 */
@Slf4j
public abstract class Application extends javafx.application.Application implements LifeCycle, StageDecorator.ActionHandler {
    @Getter(AccessLevel.PROTECTED)
    private Stage stage;
    private Scene scene;
    private final Map<Class<?>, Parent> views = new HashMap<>();
    /**
     * 自定义Application CSS
     */
    private static final String APP_STYLE = ResourceUtils.loadCss("/css/application.css");
    /**
     * 图标
     */
    public static final Image ICON = new Image(ResourceUtils.load("/assets/favicon.png").toString());

    static {
        Application.setUserAgentStylesheet(APP_STYLE);
        // 设置主题
        List<String> theme = Arrays.asList("com/sun/javafx/scene/control/skin/modena/modena.css", APP_STYLE);
        StyleManager.getInstance().setUserAgentStylesheets(theme);
    }

    /**
     * 初始化 preloader 加载
     *
     * @throws Exception 加载失败
     */
    @Override
    public final void init() throws Exception {
        StageHelper.setPrimary(stage, true);
        this.stage.getIcons().setAll(ICON);
        this.scene = new Scene(new Pane(), Color.TRANSPARENT);
        this.stage.setScene(scene);
        this.stage.onHiddenProperty().addListener(e -> onHidden());
        this.stage.onShowingProperty().addListener(e -> onShow());
        navigate(getIndexView());
    }

    public final void start() {
        this.stage.show();
    }

    /**
     * 入口
     *
     * @param stage 舞台
     */
    @Override
    public final void start(Stage stage) {
        this.start();
    }

    /**
     * 获取试图,只会调用一次
     *
     * @return 视图
     * @throws Exception 异常
     */
    public abstract Class<? extends SceneView> getIndexView() throws Exception;


    public <T extends SceneView> void navigate(Class<T> viewClass) {
        Parent view = views.get(viewClass);
        if (view == null) {
            view = load(viewClass);
            views.put(viewClass, view);
        }
        this.stage.setMinWidth(view.minWidth(-1));
        this.stage.setMinHeight(view.minHeight(-1));
        this.scene.setRoot(view);
        this.scene.setFill(Color.TRANSPARENT);
    }

    /**
     * 加载view时调用
     *
     * @param viewClass
     * @param <T>
     * @return
     */
    public <T extends Parent> T load(@NonNull Class<? extends SceneView> viewClass) {
        FXMLLoader loader = FxmlLoader.loadedLoader(viewClass);
        T view = loader.getRoot();
        if (view instanceof StageDecorator) {
            StageDecorator decorator = (StageDecorator) view;
            decorator.setStage(stage, loader.getController());
        }
        return view;
    }

    /**
     * 获取当前View
     *
     * @return view
     */
    @SuppressWarnings("unchecked")
    public <T extends Parent> T currentView() {
        if (this.scene == null) {
            log.error("需要在创建View之后调用");
            throw new NullPointerException();
        }
        return (T) this.scene.getRoot();
    }
}
