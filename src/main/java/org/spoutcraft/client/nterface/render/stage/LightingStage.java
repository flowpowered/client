package org.spoutcraft.client.nterface.render.stage;

import java.util.Arrays;

import org.spout.renderer.api.Creatable;
import org.spout.renderer.api.Material;
import org.spout.renderer.api.Pipeline;
import org.spout.renderer.api.Pipeline.PipelineBuilder;
import org.spout.renderer.api.data.Uniform.FloatUniform;
import org.spout.renderer.api.data.UniformHolder;
import org.spout.renderer.api.gl.FrameBuffer;
import org.spout.renderer.api.gl.FrameBuffer.AttachmentPoint;
import org.spout.renderer.api.gl.Texture;
import org.spout.renderer.api.model.Model;

import org.spoutcraft.client.nterface.render.Renderer;

/**
 *
 */
public class LightingStage extends Creatable {
    private final Renderer renderer;
    private final Material material;
    private final FrameBuffer frameBuffer;
    private Texture colorsInput;
    private Texture normalsInput;
    private Texture depthsInput;
    private Texture materialInput;
    private Texture occlusionsInput;
    private Texture shadowsInput;
    private Texture colorsOutput;
    private Pipeline pipeline;

    public LightingStage(Renderer renderer) {
        this.renderer = renderer;
        material = new Material(renderer.getProgram("lighting"));
        frameBuffer = renderer.getGLFactory().createFrameBuffer();
    }

    @Override
    public void create() {
        if (isCreated()) {
            throw new IllegalStateException("Lighting stage has already been created");
        }
        // Create the material
        material.addTexture(0, colorsInput);
        material.addTexture(1, normalsInput);
        material.addTexture(2, depthsInput);
        material.addTexture(3, materialInput);
        material.addTexture(4, occlusionsInput);
        material.addTexture(5, shadowsInput);
        final UniformHolder uniforms = material.getUniforms();
        uniforms.add(new FloatUniform("tanHalfFOV", Renderer.TAN_HALF_FOV));
        uniforms.add(new FloatUniform("aspectRatio", Renderer.ASPECT_RATIO));
        uniforms.add(renderer.getLightDirectionUniform());
        // Create the screen model
        final Model model = new Model(renderer.getScreen(), material);
        // Create the frame buffer
        frameBuffer.attach(AttachmentPoint.COLOR0, colorsOutput);
        frameBuffer.create();
        // Create the pipeline
        pipeline = new PipelineBuilder().bindFrameBuffer(frameBuffer).renderModels(Arrays.asList(model)).unbindFrameBuffer(frameBuffer).build();
        // Update state to created
        super.create();
    }

    @Override
    public void destroy() {
        checkCreated();
        if (colorsOutput.isCreated()) {
            colorsOutput.destroy();
        }
        super.destroy();
    }

    public void render() {
        checkCreated();
        pipeline.run(renderer.getContext());
    }

    public void setColorsInput(Texture texture) {
        texture.checkCreated();
        colorsInput = texture;
    }

    public void setNormalsInput(Texture texture) {
        texture.checkCreated();
        normalsInput = texture;
    }

    public void setDepthsInput(Texture texture) {
        texture.checkCreated();
        depthsInput = texture;
    }

    public void setMaterialInput(Texture texture) {
        texture.checkCreated();
        materialInput = texture;
    }

    public void setOcclusionsInput(Texture texture) {
        texture.checkCreated();
        occlusionsInput = texture;
    }

    public void setShadowsInput(Texture texture) {
        texture.checkCreated();
        shadowsInput = texture;
    }

    public Texture getColorsOutput() {
        return colorsOutput;
    }

    public void setColorsOutput(Texture texture) {
        texture.checkCreated();
        colorsOutput = texture;
    }
}
