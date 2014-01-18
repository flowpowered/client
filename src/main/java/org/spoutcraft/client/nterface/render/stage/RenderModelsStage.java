package org.spoutcraft.client.nterface.render.stage;

import org.spout.renderer.api.Creatable;
import org.spout.renderer.api.Pipeline;
import org.spout.renderer.api.Pipeline.PipelineBuilder;
import org.spout.renderer.api.gl.FrameBuffer;
import org.spout.renderer.api.gl.FrameBuffer.AttachmentPoint;
import org.spout.renderer.api.gl.GLFactory;
import org.spout.renderer.api.gl.Texture;

import org.spoutcraft.client.nterface.render.Renderer;

/**
 *
 */
public class RenderModelsStage extends Creatable {
    private final Renderer renderer;
    private final FrameBuffer frameBuffer;
    private Pipeline pipeline;
    private Texture colorsOutput;
    private Texture normalsOutput;
    private Texture depthsOutput;

    public RenderModelsStage(Renderer renderer) {
        this.renderer = renderer;
        final GLFactory glFactory = renderer.getGLFactory();
        frameBuffer = glFactory.createFrameBuffer();
    }

    @Override
    public void create() {
        if (isCreated()) {
            throw new IllegalStateException("Render models stage has already been created");
        }
        // Create the frame buffer
        frameBuffer.attach(AttachmentPoint.COLOR0, colorsOutput);
        frameBuffer.attach(AttachmentPoint.COLOR1, normalsOutput);
        frameBuffer.attach(AttachmentPoint.DEPTH, depthsOutput);
        frameBuffer.create();
        // Create the pipeline
        pipeline = new PipelineBuilder().bindFrameBuffer(frameBuffer).clearBuffer().renderModels(renderer.getModels()).unbindFrameBuffer(frameBuffer).build();
        // Update the state to created
        super.create();
    }

    @Override
    public void destroy() {
        checkCreated();
        frameBuffer.destroy();
        super.destroy();
    }

    public void render() {
        checkCreated();
        pipeline.run(renderer.getContext());
    }

    public Texture getColorsOutput() {
        return colorsOutput;
    }

    public void setColorsOutput(Texture texture) {
        texture.checkCreated();
        colorsOutput = texture;
    }

    public Texture getNormalsOutput() {
        return normalsOutput;
    }

    public void setNormalsOutput(Texture texture) {
        texture.checkCreated();
        normalsOutput = texture;
    }

    public Texture getDepthsOutput() {
        return depthsOutput;
    }

    public void setDepthsOutput(Texture texture) {
        texture.checkCreated();
        depthsOutput = texture;
    }
}
