package org.spoutcraft.client.nterface.render.stage;

import org.spout.renderer.api.Creatable;
import org.spout.renderer.api.Pipeline;
import org.spout.renderer.api.Pipeline.PipelineBuilder;

import org.spoutcraft.client.nterface.render.Renderer;

/**
 *
 */
public class RenderGUIStage extends Creatable {
    private final Renderer renderer;
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
        pipeline = new PipelineBuilder().useCamera(renderer.getGUICamera()).clearBuffer().renderModels(renderer.getGUIModels()).useCamera(renderer.getCamera()).updateDisplay().build();
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
}
