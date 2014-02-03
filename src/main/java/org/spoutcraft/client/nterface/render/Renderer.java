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
package org.spoutcraft.client.nterface.render;

import javax.imageio.ImageIO;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.flowpowered.commons.TPSMonitor;
import com.flowpowered.math.imaginary.Quaternionf;
import com.flowpowered.math.matrix.Matrix3f;
import com.flowpowered.math.matrix.Matrix4f;
import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector3f;

import org.lwjgl.opengl.GLContext;

import org.spout.renderer.api.Camera;
import org.spout.renderer.api.GLImplementation;
import org.spout.renderer.api.GLVersioned.GLVersion;
import org.spout.renderer.api.Material;
import org.spout.renderer.api.data.Color;
import org.spout.renderer.api.data.Uniform.ColorUniform;
import org.spout.renderer.api.data.Uniform.FloatUniform;
import org.spout.renderer.api.data.Uniform.Matrix4Uniform;
import org.spout.renderer.api.data.Uniform.Vector3Uniform;
import org.spout.renderer.api.data.UniformHolder;
import org.spout.renderer.api.gl.Context;
import org.spout.renderer.api.gl.Context.Capability;
import org.spout.renderer.api.gl.GLFactory;
import org.spout.renderer.api.gl.Program;
import org.spout.renderer.api.gl.Shader;
import org.spout.renderer.api.gl.Texture.Format;
import org.spout.renderer.api.gl.VertexArray;
import org.spout.renderer.api.model.Model;
import org.spout.renderer.api.model.StringModel;
import org.spout.renderer.api.util.MeshGenerator;
import org.spout.renderer.api.util.Rectangle;
import org.spout.renderer.lwjgl.LWJGLUtil;

import org.spoutcraft.client.nterface.Interface;
import org.spoutcraft.client.nterface.render.graph.node.GaussianBlurNode;
import org.spoutcraft.client.nterface.render.graph.node.LightingNode;
import org.spoutcraft.client.nterface.render.graph.node.RenderGUINode;
import org.spoutcraft.client.nterface.render.graph.node.RenderModelsNode;
import org.spoutcraft.client.nterface.render.graph.node.RenderTransparentModelsNode;
import org.spoutcraft.client.nterface.render.graph.node.SSAONode;
import org.spoutcraft.client.nterface.render.graph.node.ShadowMappingNode;

/**
 * The default renderer. Support OpenGL 2.1 and 3.2. Can render fully textured models with normal and specular mapping, ambient occlusion (SSAO), shadow mapping, Phong shading, motion blur and edge
 * detection anti-aliasing. The default OpenGL version is 3.2.
 */
public class Renderer {
    private static final String WINDOW_TITLE = "Spoutcraft";
    public static final Vector2f WINDOW_SIZE = new Vector2f(1200, 800);
    public static final Vector2f SHADOW_SIZE = new Vector2f(2048, 2048);
    public static final float ASPECT_RATIO = WINDOW_SIZE.getX() / WINDOW_SIZE.getY();
    public static final float FIELD_OF_VIEW = 60;
    public static final float TAN_HALF_FOV = (float) Math.tan(Math.toRadians(FIELD_OF_VIEW) / 2);
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 1000;
    public static final Vector2f PROJECTION = new Vector2f(FAR_PLANE / (FAR_PLANE - NEAR_PLANE), (-FAR_PLANE * NEAR_PLANE) / (FAR_PLANE - NEAR_PLANE));
    private static final DateFormat SCREENSHOT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    // Settings
    private boolean cullBackFaces = true;
    private Color solidModelColor = Color.WHITE;
    // Effect uniforms
    private final Vector3Uniform lightDirectionUniform = new Vector3Uniform("lightDirection", Vector3f.FORWARD);
    private final Matrix4Uniform previousViewMatrixUniform = new Matrix4Uniform("previousViewMatrix", new Matrix4f());
    private final Matrix4Uniform previousProjectionMatrixUniform = new Matrix4Uniform("previousProjectionMatrix", new Matrix4f());
    private final FloatUniform blurStrengthUniform = new FloatUniform("blurStrength", 1);
    // OpenGL version, factory and context
    private GLVersion glVersion;
    private GLFactory glFactory;
    private Context context;
    // Shader programs
    private final Map<String, Program> programs = new HashMap<>();
    // Included materials
    private Material solidMaterial;
    private Material transparencyMaterial;
    private Material screenMaterial;
    // Vertex array for stage screens, is reused
    private VertexArray screenVertexArray;
    // Stages
    private RenderModelsNode renderModelsNode;
    private ShadowMappingNode shadowMappingNode;
    private SSAONode ssaoNode;
    private GaussianBlurNode gaussianBlurNode;
    private LightingNode lightingNode;
    private RenderTransparentModelsNode renderTransparentModelsNode;
    private RenderGUINode renderGUINode;
    // FPS monitor
    private final TPSMonitor fpsMonitor = new TPSMonitor();
    private StringModel fpsMonitorModel;
    private boolean fpsMonitorStarted = false;

    public Renderer() {
        // Set the default OpenGL version to GL32
        setGLVersion(GLVersion.GL32);
    }

    /**
     * Creates the OpenGL context and initializes the internal resources for the renderer
     */
    public void init() {
        initContext();
        initPrograms();
        initVertexArrays();
        initEffects();
        initMaterials();
        addDefaultObjects();
    }

    private void initContext() {
        context = glFactory.createContext();
        context.setWindowTitle(WINDOW_TITLE);
        context.setWindowSize(WINDOW_SIZE);
        context.create();
        context.setClearColor(new Color(0, 0, 0, 0));
        if (cullBackFaces) {
            context.enableCapability(Capability.CULL_FACE);
        }
        context.enableCapability(Capability.DEPTH_TEST);
        if (glVersion == GLVersion.GL32 || GLContext.getCapabilities().GL_ARB_depth_clamp) {
            context.enableCapability(Capability.DEPTH_CLAMP);
        }
        final UniformHolder uniforms = context.getUniforms();
        uniforms.add(previousViewMatrixUniform);
        uniforms.add(previousProjectionMatrixUniform);
    }

    private void initEffects() {
        final int blurSize = 5;
        // Render models
        renderModelsNode = new RenderModelsNode(this);
        renderModelsNode.create();
        // Shadows
        shadowMappingNode = new ShadowMappingNode(this);
        shadowMappingNode.setNormalsInput(renderModelsNode.getVertexNormalsOutput());
        shadowMappingNode.setDepthsInput(renderModelsNode.getDepthsOutput());
        shadowMappingNode.setKernelSize(8);
        shadowMappingNode.setNoiseSize(blurSize);
        shadowMappingNode.setBias(0.005f);
        shadowMappingNode.setRadius(0.0004f);
        shadowMappingNode.create();
        // SSAO
        ssaoNode = new SSAONode(this);
        ssaoNode.setNormalsInput(renderModelsNode.getNormalsOutput());
        ssaoNode.setDepthsInput(renderModelsNode.getDepthsOutput());
        ssaoNode.setKernelSize(8);
        ssaoNode.setNoiseSize(blurSize);
        ssaoNode.setRadius(0.5f);
        ssaoNode.setThreshold(0.15f);
        ssaoNode.setPower(2);
        ssaoNode.create();
        // Lighting
        lightingNode = new LightingNode(this, null, "lighting");
        lightingNode.setColorsInput(renderModelsNode.getColorsOutput());
        lightingNode.setNormalsInput(renderModelsNode.getNormalsOutput());
        lightingNode.setDepthsInput(renderModelsNode.getDepthsOutput());
        lightingNode.setMaterialsInput(renderModelsNode.getMaterialsOutput());
        lightingNode.setOcclusionsInput(ssaoNode.getOcclusionsOutput());
        lightingNode.setShadowsInput(shadowMappingNode.getShadowsOutput());
        lightingNode.create();
        // Gaussian blur
        gaussianBlurNode = new GaussianBlurNode(this);
        gaussianBlurNode.setColorsInput(lightingNode.getColorsOutput());
        gaussianBlurNode.setKernelSize(blurSize);
        gaussianBlurNode.create();
        // Transparent models
        renderTransparentModelsNode = new RenderTransparentModelsNode(this);
        renderTransparentModelsNode.setDepthsInput(renderModelsNode.getDepthsOutput());
        renderTransparentModelsNode.setColorsInput(gaussianBlurNode.getColorsOutput());
        renderTransparentModelsNode.create();
        // Render GUI
        renderGUINode = new RenderGUINode(this);
        renderGUINode.create();
    }

    private void initPrograms() {
        loadProgram("solid");
        loadProgram("textured");
        loadProgram("font");
        loadProgram("ssao");
        loadProgram("shadow");
        loadProgram("gaussianBlur");
        loadProgram("lighting");
        loadProgram("motionBlur");
        loadProgram("edaa");
        loadProgram("weightedSum");
        loadProgram("transparencyBlending");
        loadProgram("screen");
    }

    private void loadProgram(String name) {
        final String shaderPath = "/shaders/" + glVersion.toString().toLowerCase() + "/" + name;
        final Shader vertex = glFactory.createShader();
        vertex.setSource(Renderer.class.getResourceAsStream(shaderPath + ".vert"));
        vertex.create();
        final Shader fragment = glFactory.createShader();
        fragment.setSource(Renderer.class.getResourceAsStream(shaderPath + ".frag"));
        fragment.create();
        final Program program = glFactory.createProgram();
        program.addShader(vertex);
        program.addShader(fragment);
        program.create();
        programs.put(name, program);
    }

    private void initMaterials() {
        UniformHolder uniforms;
        // Solid material
        solidMaterial = new Material(programs.get("solid"));
        uniforms = solidMaterial.getUniforms();
        uniforms.add(new FloatUniform("diffuseIntensity", 0.8f));
        uniforms.add(new FloatUniform("specularIntensity", 0.5f));
        uniforms.add(new FloatUniform("ambientIntensity", 0.2f));
        uniforms.add(new FloatUniform("shininess", 0.15f));
        // Transparency material
        transparencyMaterial = new Material(programs.get("weightedSum"));
        uniforms = transparencyMaterial.getUniforms();
        uniforms.add(lightDirectionUniform);
        uniforms.add(new FloatUniform("diffuseIntensity", 0.8f));
        uniforms.add(new FloatUniform("specularIntensity", 1));
        uniforms.add(new FloatUniform("ambientIntensity", 0.2f));
        uniforms.add(new FloatUniform("shininess", 0.8f));
        // Screen material
        screenMaterial = new Material(programs.get("screen"));
        screenMaterial.addTexture(0, renderTransparentModelsNode.getColorsInput());
    }

    private void initVertexArrays() {
        screenVertexArray = glFactory.createVertexArray();
        screenVertexArray.setData(MeshGenerator.generateTexturedPlane(null, new Vector2f(2, 2)));
        screenVertexArray.create();
    }

    private void addDefaultObjects() {
        addScreen();
        addFPSMonitor();

        final VertexArray sphere = glFactory.createVertexArray();
        sphere.setData(MeshGenerator.generateSphere(null, 5));
        sphere.create();
        final Model model1 = new Model(sphere, transparencyMaterial);
        model1.setPosition(new Vector3f(0, 22, -6));
        model1.getUniforms().add(new ColorUniform("modelColor", new Color(1, 0, 0, 0.3)));
        addTransparentModel(model1);
        final Model model2 = model1.getInstance();
        model2.setPosition(new Vector3f(0, 22, 6));
        model2.getUniforms().add(new ColorUniform("modelColor", new Color(0, 0, 1, 0.7)));
        addTransparentModel(model2);
    }

    private void addScreen() {
        renderGUINode.addModel(new Model(screenVertexArray, screenMaterial));
    }

    private void addFPSMonitor() {
        final Font ubuntu;
        try {
            ubuntu = Font.createFont(Font.TRUETYPE_FONT, Renderer.class.getResourceAsStream("/fonts/ubuntu-r.ttf"));
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            return;
        }
        final StringModel sandboxModel = new StringModel(glFactory, programs.get("font"), "ClientWIPFPS0123456789-: ", ubuntu.deriveFont(Font.PLAIN, 15), WINDOW_SIZE.getFloorX());
        final float aspect = 1 / ASPECT_RATIO;
        sandboxModel.setPosition(new Vector3f(0.005, aspect / 2 + 0.315, -0.1));
        sandboxModel.setString("Client - WIP");
        renderGUINode.addModel(sandboxModel);
        final StringModel fpsModel = sandboxModel.getInstance();
        fpsModel.setPosition(new Vector3f(0.005, aspect / 2 + 0.285, -0.1));
        fpsModel.setString("FPS: " + fpsMonitor.getTPS());
        renderGUINode.addModel(fpsModel);
        fpsMonitorModel = fpsModel;
    }

    /**
     * Destroys the renderer internal resources and the OpenGL context.
     */
    public void dispose() {
        disposeEffects();
        disposePrograms();
        disposeVertexArrays();
        disposeContext();
        fpsMonitorStarted = false;
    }

    private void disposeContext() {
        context.destroy();
    }

    private void disposeEffects() {
        renderModelsNode.destroy();
        shadowMappingNode.destroy();
        ssaoNode.destroy();
        lightingNode.destroy();
        gaussianBlurNode.destroy();
        renderTransparentModelsNode.destroy();
        renderGUINode.destroy();
    }

    private void disposePrograms() {
        for (Program program : programs.values()) {
            for (Shader shader : program.getShaders()) {
                shader.destroy();
            }
            program.destroy();
        }
    }

    private void disposeVertexArrays() {
        screenVertexArray.destroy();
    }

    /**
     * Renders the models to the window.
     */
    public void render() {
        if (!fpsMonitorStarted) {
            fpsMonitor.start();
            fpsMonitorStarted = true;
        }
        // Update the current frame uniforms
        final Camera camera = renderModelsNode.getCamera();
        blurStrengthUniform.set((float) fpsMonitor.getTPS() / Interface.TPS);
        // Render
        renderModelsNode.render();
        shadowMappingNode.render();
        ssaoNode.render();
        lightingNode.render();
        gaussianBlurNode.render();
        renderTransparentModelsNode.render();
        renderGUINode.render();
        // Update the previous frame uniforms
        setPreviousModelMatrices();
        previousViewMatrixUniform.set(camera.getViewMatrix());
        previousProjectionMatrixUniform.set(camera.getProjectionMatrix());
        // Update the FPS monitor
        updateFPSMonitor();
    }

    private void setPreviousModelMatrices() {
        for (Model model : renderModelsNode.getModels()) {
            model.getUniforms().getMatrix4("previousModelMatrix").set(model.getMatrix());
        }
        for (Model model : renderTransparentModelsNode.getModels()) {
            model.getUniforms().getMatrix4("previousModelMatrix").set(model.getMatrix());
        }
    }

    private void updateFPSMonitor() {
        fpsMonitor.update();
        fpsMonitorModel.setString("FPS: " + fpsMonitor.getTPS());
    }

    public GLFactory getGLFactory() {
        return glFactory;
    }

    public GLVersion getGLVersion() {
        return glVersion;
    }

    /**
     * Sets the OpenGL version. Must be done before initializing the renderer.
     *
     * @param version The OpenGL version to use
     */
    public void setGLVersion(GLVersion version) {
        switch (version) {
            case GL20:
            case GL21:
                glFactory = GLImplementation.get(LWJGLUtil.GL21_IMPL);
                glVersion = GLVersion.GL21;
                break;
            case GL30:
            case GL31:
            case GL32:
                glFactory = GLImplementation.get(LWJGLUtil.GL32_IMPL);
                glVersion = GLVersion.GL32;
        }
    }

    public Context getContext() {
        return context;
    }

    public Program getProgram(String name) {
        return programs.get(name);
    }

    public VertexArray getScreen() {
        return screenVertexArray;
    }

    public RenderModelsNode getRenderModelsNode() {
        return renderModelsNode;
    }

    public RenderGUINode getRenderGUINode() {
        return renderGUINode;
    }

    public Vector3Uniform getLightDirectionUniform() {
        return lightDirectionUniform;
    }

    /**
     * Sets whether or not to cull the back faces of the geometry.
     *
     * @param cull Whether or not to cull the back faces
     */
    public void setCullBackFaces(boolean cull) {
        cullBackFaces = cull;
    }

    /**
     * Sets the color of solid untextured objects.
     *
     * @param color The solid color
     */
    public void setSolidColor(Color color) {
        solidModelColor = color;
    }

    /**
     * Updates the light direction and camera bounds to ensure that shadows are casted inside the cuboid defined by size.
     *
     * @param direction The light direction
     * @param position The light camera position
     * @param size The size of the cuboid that must have shadows
     */
    public void updateLight(Vector3f direction, Vector3f position, Vector3f size) {
        // Set the direction uniform
        direction = direction.normalize();
        lightDirectionUniform.set(direction);
        // Set the camera position
        final Camera camera = shadowMappingNode.getCamera();
        camera.setPosition(position);
        // Calculate the camera rotation from the direction and set
        final Quaternionf rotation = Quaternionf.fromRotationTo(Vector3f.FORWARD.negate(), direction);
        camera.setRotation(rotation);
        // Calculate the transformation from the camera bounds rotation to the identity rotation (its axis aligned space)
        final Matrix3f axisAlignTransform = Matrix3f.createRotation(rotation).invert();
        // Calculate the points of the box to completely include inside the camera bounds
        size = size.div(2);
        Vector3f p6 = size;
        Vector3f p0 = p6.negate();
        Vector3f p7 = new Vector3f(-size.getX(), size.getY(), size.getZ());
        Vector3f p1 = p7.negate();
        Vector3f p4 = new Vector3f(-size.getX(), size.getY(), -size.getZ());
        Vector3f p2 = p4.negate();
        Vector3f p5 = new Vector3f(size.getX(), size.getY(), -size.getZ());
        Vector3f p3 = p5.negate();
        // Transform those points to the axis aligned space of the camera bounds
        p0 = axisAlignTransform.transform(p0);
        p1 = axisAlignTransform.transform(p1);
        p2 = axisAlignTransform.transform(p2);
        p3 = axisAlignTransform.transform(p3);
        p4 = axisAlignTransform.transform(p4);
        p5 = axisAlignTransform.transform(p5);
        p6 = axisAlignTransform.transform(p6);
        p7 = axisAlignTransform.transform(p7);
        // Calculate the new camera bounds so that the box is fully included in those bounds
        final Vector3f low = p0.min(p1).min(p2).min(p3)
                .min(p4).min(p5).min(p6).min(p7);
        final Vector3f high = p0.max(p1).max(p2).max(p3)
                .max(p4).max(p5).max(p6).max(p7);
        // Calculate the size of the new camera bounds
        size = high.sub(low).div(2);
        // Update the camera to the new bounds
        camera.setProjection(Matrix4f.createOrthographic(size.getX(), -size.getX(), size.getY(), -size.getY(), -size.getZ(), size.getZ()));
    }

    /**
     * Adds a model to be rendered as a solid.
     *
     * @param model The model
     */
    public void addSolidModel(Model model) {
        model.setMaterial(solidMaterial);
        model.getUniforms().add(new ColorUniform("modelColor", new Color(Math.random(), Math.random(), Math.random(), 1)));
        renderModelsNode.addModel(model);
    }

    /**
     * Adds a model to be rendered as partially transparent.
     *
     * @param model The transparent model
     */
    public void addTransparentModel(Model model) {
        model.setMaterial(transparencyMaterial);
        renderTransparentModelsNode.addModel(model);
    }

    /**
     * Saves a screenshot (PNG) to the directory where the program is currently running, with the current date as the file name.
     *
     * @param outputDir The directory in which to output the file
     */
    public void saveScreenshot(File outputDir) {
        final ByteBuffer buffer = context.readCurrentFrame(new Rectangle(Vector2f.ZERO, WINDOW_SIZE), Format.RGB);
        final int width = context.getWindowWidth();
        final int height = context.getWindowHeight();
        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        final byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                final int srcIndex = (x + y * width) * 3;
                final int destIndex = (x + (height - y - 1) * width) * 3;
                data[destIndex + 2] = buffer.get(srcIndex);
                data[destIndex + 1] = buffer.get(srcIndex + 1);
                data[destIndex] = buffer.get(srcIndex + 2);
            }
        }
        try {
            ImageIO.write(image, "PNG", new File(outputDir, SCREENSHOT_DATE_FORMAT.format(Calendar.getInstance().getTime()) + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
