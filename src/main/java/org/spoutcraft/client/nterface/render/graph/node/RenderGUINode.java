/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013-2014 Spoutcraft <http://spoutcraft.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spoutcraft.client.nterface.render.graph.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spout.renderer.api.Camera;
import org.spout.renderer.api.Material;
import org.spout.renderer.api.Pipeline;
import org.spout.renderer.api.Pipeline.PipelineBuilder;
import org.spout.renderer.api.data.Uniform.Matrix4Uniform;
import org.spout.renderer.api.gl.Texture;
import org.spout.renderer.api.model.Model;
import org.spout.renderer.api.util.Rectangle;

import org.spoutcraft.client.nterface.render.graph.RenderGraph;

/**
 *
 */
public class RenderGUINode extends GraphNode {
    private final Material material;
    private final Camera camera;
    private final List<Model> models = new ArrayList<>();
    private final Pipeline pipeline;
    private final Rectangle screenSize = new Rectangle();

    public RenderGUINode(RenderGraph graph, String name) {
        super(graph, name);
        camera = Camera.createOrthographic(1, 0, 1 / graph.getAspectRatio(), 0, graph.getNearPlane(), graph.getFarPlane());
        screenSize.setSize(graph.getWindowSize());
        material = new Material(graph.getProgram("screen"));
        final Model model = new Model(graph.getScreen(), material);
        pipeline = new PipelineBuilder().useCamera(camera).useViewPort(screenSize).clearBuffer().renderModels(Arrays.asList(model)).renderModels(models).build();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void render() {
        pipeline.run(graph.getContext());
    }

    @Input("colors")
    public void setColorsInput(Texture colorsInput) {
        material.addTexture(0, colorsInput);
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
