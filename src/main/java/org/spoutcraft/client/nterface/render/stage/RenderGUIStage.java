package org.spoutcraft.client.nterface.render.stage;

import java.util.ArrayList;
import java.util.List;

import org.spout.renderer.api.Camera;
import org.spout.renderer.api.Creatable;
import org.spout.renderer.api.Pipeline;
import org.spout.renderer.api.Pipeline.PipelineBuilder;
import org.spout.renderer.api.data.Uniform.Matrix4Uniform;
import org.spout.renderer.api.model.Model;

import org.spoutcraft.client.nterface.render.Renderer;

/**
 *
 */
public class RenderGUIStage extends Creatable {
    private final Renderer renderer;
    private final Camera camera = Camera.createOrthographic(1, 0, 1 / Renderer.ASPECT_RATIO, 0, Renderer.NEAR_PLANE, Renderer.FAR_PLANE);
    private final List<Model> models = new ArrayList<>();
    private Pipeline pipeline;

    public RenderGUIStage(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void create() {
        if (isCreated()) {
            throw new IllegalStateException("Render models stage has already been created");
        }
        // Create the pipeline
        pipeline = new PipelineBuilder().useCamera(camera).clearBuffer().renderModels(models).updateDisplay().build();
        // Update state to created
        super.create();
    }

    @Override
    public void destroy() {
        checkCreated();
        super.destroy();
    }

    public void render() {
        checkCreated();
        pipeline.run(renderer.getContext());
    }

    public Camera getCamera() {
        return camera;
    }

    /**
     * Adds a model to the renderer.
     *
     * @param model The model to add
     */
    public void addModel(Model model) {
        model.getUniforms().add(new Matrix4Uniform("previousModelMatrix", model.getMatrix()));
        models.add(model);
    }

    /**
     * Removes a model from the renderer.
     *
     * @param model The model to remove
     */
    public void removeModel(Model model) {
        models.remove(model);
    }

    /**
     * Removes all the models from the renderer.
     */
    public void clearModels() {
        models.clear();
    }

    public List<Model> getModels() {
        return models;
    }
}
