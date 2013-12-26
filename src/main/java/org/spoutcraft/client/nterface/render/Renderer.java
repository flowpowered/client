/**
 * This file is part of Client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spoutcraft <http://spoutcraft.org/>
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GLContext;

import org.spout.math.GenericMath;
import org.spout.math.TrigMath;
import org.spout.math.imaginary.Quaternionf;
import org.spout.math.matrix.Matrix4f;
import org.spout.math.vector.Vector2f;
import org.spout.math.vector.Vector3f;
import org.spout.renderer.Action.RenderModelsAction;
import org.spout.renderer.Camera;
import org.spout.renderer.GLImplementation;
import org.spout.renderer.GLVersioned.GLVersion;
import org.spout.renderer.Material;
import org.spout.renderer.Pipeline;
import org.spout.renderer.Pipeline.PipelineBuilder;
import org.spout.renderer.data.Color;
import org.spout.renderer.data.Uniform.ColorUniform;
import org.spout.renderer.data.Uniform.FloatUniform;
import org.spout.renderer.data.Uniform.IntUniform;
import org.spout.renderer.data.Uniform.Matrix4Uniform;
import org.spout.renderer.data.Uniform.Vector2Uniform;
import org.spout.renderer.data.Uniform.Vector3Uniform;
import org.spout.renderer.data.UniformHolder;
import org.spout.renderer.data.VertexAttribute.DataType;
import org.spout.renderer.data.VertexData;
import org.spout.renderer.gl.Context;
import org.spout.renderer.gl.Context.Capability;
import org.spout.renderer.gl.FrameBuffer;
import org.spout.renderer.gl.FrameBuffer.AttachmentPoint;
import org.spout.renderer.gl.GLFactory;
import org.spout.renderer.gl.Program;
import org.spout.renderer.gl.Shader;
import org.spout.renderer.gl.Texture;
import org.spout.renderer.gl.Texture.CompareMode;
import org.spout.renderer.gl.Texture.FilterMode;
import org.spout.renderer.gl.Texture.Format;
import org.spout.renderer.gl.Texture.InternalFormat;
import org.spout.renderer.gl.Texture.WrapMode;
import org.spout.renderer.gl.VertexArray;
import org.spout.renderer.model.Model;
import org.spout.renderer.model.StringModel;
import org.spout.renderer.util.Rectangle;

import org.spoutcraft.client.nterface.Interface;
import org.spoutcraft.client.nterface.render.render.BlurEffect;
import org.spoutcraft.client.nterface.render.render.SSAOEffect;
import org.spoutcraft.client.nterface.render.render.ShadowMappingEffect;
import org.spoutcraft.client.util.MeshGenerator;
import org.spoutcraft.client.util.TPSMonitor;

public class Renderer {
    // CONSTANTS
    private static final String WINDOW_TITLE = "Spoutcraft";
    private static final Vector2f WINDOW_SIZE = new Vector2f(1200, 800);
    private static final Vector2f SHADOW_SIZE = new Vector2f(2048, 2048);
    private static final float ASPECT_RATIO = WINDOW_SIZE.getX() / WINDOW_SIZE.getY();
    private static final float FIELD_OF_VIEW = 60;
    private static final float TAN_HALF_FOV = (float) Math.tan(Math.toRadians(FIELD_OF_VIEW) / 2);
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 1000;
    private static final Vector2f PROJECTION = new Vector2f(FAR_PLANE / (FAR_PLANE - NEAR_PLANE), (-FAR_PLANE * NEAR_PLANE) / (FAR_PLANE - NEAR_PLANE));
    private static final DateFormat SCREENSHOT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    // SETTINGS
    private static Color backgroundColor = Color.DARK_GRAY;
    private static boolean cullBackFaces = true;
    // EFFECT UNIFORMS
    private static final Vector3Uniform lightPositionUniform = new Vector3Uniform("lightPosition", Vector3f.ZERO);
    private static final Vector3Uniform spotDirectionUniform = new Vector3Uniform("spotDirection", new Vector3f(0, 0, -1));
    private static final FloatUniform lightAttenuationUniform = new FloatUniform("lightAttenuation", 0.03f);
    private static final Matrix4Uniform inverseViewMatrixUniform = new Matrix4Uniform("inverseViewMatrix", new Matrix4f());
    private static final Matrix4Uniform lightViewMatrixUniform = new Matrix4Uniform("lightViewMatrix", new Matrix4f());
    private static final Matrix4Uniform lightProjectionMatrixUniform = new Matrix4Uniform("lightProjectionMatrix", new Matrix4f());
    private static final Matrix4Uniform previousViewMatrixUniform = new Matrix4Uniform("previousViewMatrix", new Matrix4f());
    private static final Matrix4Uniform previousProjectionMatrixUniform = new Matrix4Uniform("previousProjectionMatrix", new Matrix4f());
    private static final FloatUniform blurStrengthUniform = new FloatUniform("blurStrength", 1);
    // CAMERAS
    private static final Camera modelCamera = Camera.createPerspective(FIELD_OF_VIEW, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY(), NEAR_PLANE, FAR_PLANE);
    private static final Camera lightCamera = Camera.createPerspective((float) TrigMath.RAD_TO_DEG * Interface.SPOT_CUTOFF * 2, 1, 1, 0.1f, (float) GenericMath.length(50d, 100d));
    private static final Camera guiCamera = Camera.createOrthographic(1, 0, 1 / ASPECT_RATIO, 0, NEAR_PLANE, FAR_PLANE);
    // OPENGL VERSION AND FACTORY
    private static GLVersion glVersion;
    private static GLFactory glFactory;
    // CONTEXT
    private static Context context;
    // RENDER LISTS
    private static final List<Model> modelRenderList = new ArrayList<>();
    private static final List<Model> guiRenderList = new ArrayList<>();
    // PIPELINE
    private static Pipeline pipeline;
    // SHADERS
    private static final Map<String, Program> programs = new HashMap<>();
    // TEXTURES
    private static Texture colorsTexture;
    private static Texture normalsTexture;
    private static Texture vertexNormals;
    private static Texture materialsTexture;
    private static Texture velocitiesTexture;
    private static Texture depthsTexture;
    private static Texture lightDepthsTexture;
    private static Texture ssaoTexture;
    private static Texture shadowTexture;
    private static Texture auxRTexture;
    private static Texture auxRGBATexture;
    // MATERIALS
    private static Material solidMaterial;
    private static Material ssaoMaterial;
    private static Material blurMaterial;
    private static Material shadowMaterial;
    private static Material lightingMaterial;
    private static Material motionBlurMaterial;
    private static Material antiAliasingMaterial;
    private static Material screenMaterial;
    // FRAME BUFFERS
    private static FrameBuffer modelFrameBuffer;
    private static FrameBuffer lightModelFrameBuffer;
    private static FrameBuffer ssaoFrameBuffer;
    private static FrameBuffer blurFrameBuffer;
    private static FrameBuffer shadowFrameBuffer;
    private static FrameBuffer lightingFrameBuffer;
    private static FrameBuffer motionBlurFrameBuffer;
    private static FrameBuffer antiAliasingFrameBuffer;
    // VERTEX ARRAYS
    private static VertexArray deferredStageScreenVertexArray;
    // EFFECTS
    private static SSAOEffect ssaoEffect;
    private static ShadowMappingEffect shadowMappingEffect;
    private static BlurEffect blurEffect;
    // MODEL PROPERTIES
    private static Color solidModelColor;
    // FPS MONITOR
    private static final TPSMonitor fpsMonitor = new TPSMonitor();
    private static StringModel fpsMonitorModel;

    public static void init() {
        initContext();
        initEffects();
        initPrograms();
        initTextures();
        initMaterials();
        initFrameBuffers();
        initVertexArrays();
        initPipeline();
    }

    private static void initContext() {
        // CONTEXT
        context = glFactory.createContext();
        context.setWindowTitle(WINDOW_TITLE);
        context.setWindowSize(WINDOW_SIZE);
        context.create();
        context.setClearColor(new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 0));
        context.setCamera(modelCamera);
        if (cullBackFaces) {
            context.enableCapability(Capability.CULL_FACE);
        }
        context.enableCapability(Capability.DEPTH_TEST);
        if (glVersion == GLVersion.GL30 || GLContext.getCapabilities().GL_ARB_depth_clamp) {
            context.enableCapability(Capability.DEPTH_CLAMP);
        }
        final UniformHolder uniforms = context.getUniforms();
        uniforms.add(previousViewMatrixUniform);
        uniforms.add(previousProjectionMatrixUniform);
    }

    private static void initEffects() {
        final int blurSize = 2;
        // SSAO
        ssaoEffect = new SSAOEffect(glFactory, WINDOW_SIZE, 8, blurSize, 0.5f, 0.15f, 2);
        // SHADOW MAPPING
        shadowMappingEffect = new ShadowMappingEffect(glFactory, WINDOW_SIZE, 8, blurSize, 0.000006f, 0.0004f);
        // BLUR
        blurEffect = new BlurEffect(WINDOW_SIZE, blurSize);
    }

    private static void initPipeline() {
        PipelineBuilder pipelineBuilder = new PipelineBuilder();
        // MODEL
        pipelineBuilder = pipelineBuilder.bindFrameBuffer(modelFrameBuffer).clearBuffer().renderModels(modelRenderList).unbindFrameBuffer(modelFrameBuffer);
        // LIGHT MODEL
        pipelineBuilder = pipelineBuilder.useViewPort(new Rectangle(Vector2f.ZERO, SHADOW_SIZE)).useCamera(lightCamera).bindFrameBuffer(lightModelFrameBuffer).clearBuffer()
                .renderModels(modelRenderList).unbindFrameBuffer(lightModelFrameBuffer).useViewPort(new Rectangle(Vector2f.ZERO, WINDOW_SIZE)).useCamera(modelCamera);
        // SSAO
        if (glVersion == GLVersion.GL30 || GLContext.getCapabilities().GL_ARB_depth_clamp) {
            pipelineBuilder = pipelineBuilder.disableCapabilities(Capability.DEPTH_CLAMP);
        }
        pipelineBuilder = pipelineBuilder.disableCapabilities(Capability.DEPTH_TEST).doAction(new DoDeferredStageAction(ssaoFrameBuffer, deferredStageScreenVertexArray, ssaoMaterial));
        // SHADOW
        pipelineBuilder = pipelineBuilder.doAction(new DoDeferredStageAction(shadowFrameBuffer, deferredStageScreenVertexArray, shadowMaterial));
        // BLUR
        pipelineBuilder = pipelineBuilder.doAction(new DoDeferredStageAction(blurFrameBuffer, deferredStageScreenVertexArray, blurMaterial));
        // LIGHTING
        pipelineBuilder = pipelineBuilder.doAction(new DoDeferredStageAction(lightingFrameBuffer, deferredStageScreenVertexArray, lightingMaterial));
        // MOTION BLUR
        pipelineBuilder = pipelineBuilder.doAction(new DoDeferredStageAction(motionBlurFrameBuffer, deferredStageScreenVertexArray, motionBlurMaterial));
        // ANTI ALIASING
        pipelineBuilder = pipelineBuilder.doAction(new DoDeferredStageAction(antiAliasingFrameBuffer, deferredStageScreenVertexArray, antiAliasingMaterial))
                .unbindFrameBuffer(antiAliasingFrameBuffer).enableCapabilities(Capability.DEPTH_TEST);
        if (glVersion == GLVersion.GL30 || GLContext.getCapabilities().GL_ARB_depth_clamp) {
            pipelineBuilder = pipelineBuilder.enableCapabilities(Capability.DEPTH_CLAMP);
        }
        // GUI
        pipelineBuilder = pipelineBuilder.useCamera(guiCamera).clearBuffer().renderModels(guiRenderList).useCamera(modelCamera).updateDisplay();
        pipeline = pipelineBuilder.build();
    }

    private static void initPrograms() {
        // SOLID
        loadProgram("solid");
        // TEXTURED
        loadProgram("textured");
        /// FONT
        loadProgram("font");
        // SSAO
        loadProgram("ssao");
        // SHADOW
        loadProgram("shadow");
        // BLUR
        loadProgram("blur");
        // LIGHTING
        loadProgram("lighting");
        // MOTION BLUR
        loadProgram("motionBlur");
        // ANTI ALIASING
        loadProgram("edaa");
        // SCREEN
        loadProgram("screen");
    }

    private static void loadProgram(String name) {
        final String shaderPath = "/shaders/" + glVersion.toString().toLowerCase() + "/" + name;
        // SHADERS
        final Shader vert = glFactory.createShader();
        vert.setSource(Renderer.class.getResourceAsStream(shaderPath + ".vert"));
        vert.create();
        final Shader frag = glFactory.createShader();
        frag.setSource(Renderer.class.getResourceAsStream(shaderPath + ".frag"));
        frag.create();
        // PROGRAM
        final Program program = glFactory.createProgram();
        program.addShader(vert);
        program.addShader(frag);
        program.create();
        programs.put(name, program);
    }

    private static void initTextures() {
        // COLORS
        colorsTexture = glFactory.createTexture();
        colorsTexture.setFormat(Format.RGBA);
        colorsTexture.setInternalFormat(InternalFormat.RGBA8);
        colorsTexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        colorsTexture.setMagFilter(FilterMode.LINEAR);
        colorsTexture.setMinFilter(FilterMode.LINEAR);
        colorsTexture.create();
        // NORMALS
        normalsTexture = glFactory.createTexture();
        normalsTexture.setFormat(Format.RGBA);
        normalsTexture.setInternalFormat(InternalFormat.RGBA8);
        normalsTexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        normalsTexture.create();
        // VERTEX NORMALS
        vertexNormals = glFactory.createTexture();
        vertexNormals.setFormat(Format.RGBA);
        vertexNormals.setInternalFormat(InternalFormat.RGBA8);
        vertexNormals.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        vertexNormals.create();
        // MATERIALS
        materialsTexture = glFactory.createTexture();
        materialsTexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        materialsTexture.create();
        // VELOCITIES
        velocitiesTexture = glFactory.createTexture();
        velocitiesTexture.setFormat(Format.RG);
        velocitiesTexture.setInternalFormat(InternalFormat.RG16F);
        velocitiesTexture.setComponentType(DataType.HALF_FLOAT);
        velocitiesTexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        velocitiesTexture.create();
        // DEPTHS
        depthsTexture = glFactory.createTexture();
        depthsTexture.setFormat(Format.DEPTH);
        depthsTexture.setInternalFormat(InternalFormat.DEPTH_COMPONENT32);
        depthsTexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        depthsTexture.setWrapS(WrapMode.CLAMP_TO_EDGE);
        depthsTexture.setWrapT(WrapMode.CLAMP_TO_EDGE);
        depthsTexture.create();
        // LIGHT DEPTHS
        lightDepthsTexture = glFactory.createTexture();
        lightDepthsTexture.setFormat(Format.DEPTH);
        lightDepthsTexture.setInternalFormat(InternalFormat.DEPTH_COMPONENT32);
        lightDepthsTexture.setImageData(null, SHADOW_SIZE.getFloorX(), SHADOW_SIZE.getFloorY());
        lightDepthsTexture.setWrapS(WrapMode.CLAMP_TO_BORDER);
        lightDepthsTexture.setWrapT(WrapMode.CLAMP_TO_BORDER);
        lightDepthsTexture.setMagFilter(FilterMode.LINEAR);
        lightDepthsTexture.setMinFilter(FilterMode.LINEAR);
        lightDepthsTexture.setCompareMode(CompareMode.LESS);
        lightDepthsTexture.create();
        // SSAO
        ssaoTexture = glFactory.createTexture();
        ssaoTexture.setFormat(Format.RED);
        ssaoTexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        ssaoTexture.create();
        // SHADOW
        shadowTexture = glFactory.createTexture();
        shadowTexture.setFormat(Format.RED);
        shadowTexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        shadowTexture.create();
        // AUX R
        auxRTexture = glFactory.createTexture();
        auxRTexture.setFormat(Format.RED);
        auxRTexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        auxRTexture.create();
        // AUX RGBA
        auxRGBATexture = glFactory.createTexture();
        auxRGBATexture.setFormat(Format.RGBA);
        auxRGBATexture.setInternalFormat(InternalFormat.RGBA8);
        auxRGBATexture.setImageData(null, WINDOW_SIZE.getFloorX(), WINDOW_SIZE.getFloorY());
        auxRGBATexture.setWrapS(WrapMode.CLAMP_TO_EDGE);
        auxRGBATexture.setWrapT(WrapMode.CLAMP_TO_EDGE);
        auxRGBATexture.setMagFilter(FilterMode.LINEAR);
        auxRGBATexture.setMinFilter(FilterMode.LINEAR);
        auxRGBATexture.create();
    }

    private static void initMaterials() {
        UniformHolder uniforms;
        // SOLID
        solidMaterial = createMaterial("solid");
        uniforms = solidMaterial.getUniforms();
        uniforms.add(new FloatUniform("diffuseIntensity", 0.8f));
        uniforms.add(new FloatUniform("specularIntensity", 1));
        uniforms.add(new FloatUniform("ambientIntensity", 0.2f));
        // SSAO
        ssaoMaterial = createMaterial("ssao");
        ssaoMaterial.addTexture(0, normalsTexture);
        ssaoMaterial.addTexture(1, depthsTexture);
        ssaoMaterial.addTexture(2, ssaoEffect.getNoiseTexture());
        uniforms = ssaoMaterial.getUniforms();
        uniforms.add(new Vector2Uniform("projection", PROJECTION));
        uniforms.add(new FloatUniform("tanHalfFOV", TAN_HALF_FOV));
        uniforms.add(new FloatUniform("aspectRatio", ASPECT_RATIO));
        ssaoEffect.addUniforms(uniforms);
        // SHADOW
        shadowMaterial = createMaterial("shadow");
        shadowMaterial.addTexture(0, vertexNormals);
        shadowMaterial.addTexture(1, depthsTexture);
        shadowMaterial.addTexture(2, lightDepthsTexture);
        shadowMaterial.addTexture(3, shadowMappingEffect.getNoiseTexture());
        uniforms = shadowMaterial.getUniforms();
        uniforms.add(new Vector2Uniform("projection", PROJECTION));
        uniforms.add(new FloatUniform("tanHalfFOV", TAN_HALF_FOV));
        uniforms.add(new FloatUniform("aspectRatio", ASPECT_RATIO));
        uniforms.add(lightPositionUniform);
        uniforms.add(inverseViewMatrixUniform);
        uniforms.add(lightViewMatrixUniform);
        uniforms.add(lightProjectionMatrixUniform);
        shadowMappingEffect.addUniforms(uniforms);
        // BLUR
        blurMaterial = createMaterial("blur");
        blurMaterial.addTexture(0, auxRTexture);
        blurMaterial.addTexture(1, auxRGBATexture);
        uniforms = blurMaterial.getUniforms();
        blurEffect.addUniforms(uniforms);
        // LIGHTING
        lightingMaterial = createMaterial("lighting");
        lightingMaterial.addTexture(0, colorsTexture);
        lightingMaterial.addTexture(1, normalsTexture);
        lightingMaterial.addTexture(2, depthsTexture);
        lightingMaterial.addTexture(3, materialsTexture);
        lightingMaterial.addTexture(4, ssaoTexture);
        lightingMaterial.addTexture(5, shadowTexture);
        uniforms = lightingMaterial.getUniforms();
        uniforms.add(new Vector2Uniform("projection", PROJECTION));
        uniforms.add(new FloatUniform("tanHalfFOV", TAN_HALF_FOV));
        uniforms.add(new FloatUniform("aspectRatio", ASPECT_RATIO));
        uniforms.add(lightPositionUniform);
        uniforms.add(lightAttenuationUniform);
        uniforms.add(new FloatUniform("spotCutoff", TrigMath.cos(Interface.SPOT_CUTOFF)));
        uniforms.add(spotDirectionUniform);
        // MOTION BLUR
        motionBlurMaterial = createMaterial("motionBlur");
        motionBlurMaterial.addTexture(0, auxRGBATexture);
        motionBlurMaterial.addTexture(1, velocitiesTexture);
        uniforms = motionBlurMaterial.getUniforms();
        uniforms.add(new Vector2Uniform("resolution", WINDOW_SIZE));
        uniforms.add(new IntUniform("sampleCount", 8));
        uniforms.add(blurStrengthUniform);
        // ANTI ALIASING
        antiAliasingMaterial = createMaterial("edaa");
        antiAliasingMaterial.addTexture(0, colorsTexture);
        antiAliasingMaterial.addTexture(1, vertexNormals);
        antiAliasingMaterial.addTexture(2, depthsTexture);
        uniforms = antiAliasingMaterial.getUniforms();
        uniforms.add(new Vector2Uniform("projection", PROJECTION));
        uniforms.add(new Vector2Uniform("resolution", WINDOW_SIZE));
        uniforms.add(new FloatUniform("maxSpan", 8));
        uniforms.add(new Vector2Uniform("barriers", new Vector2f(0.8f, 0.5f)));
        uniforms.add(new Vector2Uniform("weights", new Vector2f(0.25f, 0.6f)));
        uniforms.add(new FloatUniform("kernel", 0.75f));
        // SCREEN
        screenMaterial = createMaterial("screen");
        screenMaterial.addTexture(0, auxRGBATexture);
    }

    private static Material createMaterial(String program) {
        return new Material(programs.get(program));
    }

    private static void initFrameBuffers() {
        // MODEL
        modelFrameBuffer = glFactory.createFrameBuffer();
        modelFrameBuffer.attach(AttachmentPoint.COLOR0, colorsTexture);
        modelFrameBuffer.attach(AttachmentPoint.COLOR1, normalsTexture);
        modelFrameBuffer.attach(AttachmentPoint.COLOR2, vertexNormals);
        modelFrameBuffer.attach(AttachmentPoint.COLOR3, materialsTexture);
        modelFrameBuffer.attach(AttachmentPoint.COLOR4, velocitiesTexture);
        modelFrameBuffer.attach(AttachmentPoint.DEPTH, depthsTexture);
        modelFrameBuffer.create();
        // LIGHT MODEL
        lightModelFrameBuffer = glFactory.createFrameBuffer();
        lightModelFrameBuffer.attach(AttachmentPoint.DEPTH, lightDepthsTexture);
        lightModelFrameBuffer.create();
        // SSAO
        ssaoFrameBuffer = glFactory.createFrameBuffer();
        ssaoFrameBuffer.attach(AttachmentPoint.COLOR0, auxRTexture);
        ssaoFrameBuffer.create();
        // SHADOW
        shadowFrameBuffer = glFactory.createFrameBuffer();
        shadowFrameBuffer.attach(AttachmentPoint.COLOR0, auxRGBATexture);
        shadowFrameBuffer.create();
        // BLUR
        blurFrameBuffer = glFactory.createFrameBuffer();
        blurFrameBuffer.attach(AttachmentPoint.COLOR0, ssaoTexture);
        blurFrameBuffer.attach(AttachmentPoint.COLOR1, shadowTexture);
        blurFrameBuffer.create();
        // LIGHTING
        lightingFrameBuffer = glFactory.createFrameBuffer();
        lightingFrameBuffer.attach(AttachmentPoint.COLOR0, auxRGBATexture);
        lightingFrameBuffer.create();
        // MOTION BLUR
        motionBlurFrameBuffer = glFactory.createFrameBuffer();
        motionBlurFrameBuffer.attach(AttachmentPoint.COLOR0, colorsTexture);
        motionBlurFrameBuffer.create();
        // ANTI ALIASING
        antiAliasingFrameBuffer = glFactory.createFrameBuffer();
        antiAliasingFrameBuffer.attach(AttachmentPoint.COLOR0, auxRGBATexture);
        antiAliasingFrameBuffer.create();
    }

    private static void initVertexArrays() {
        // DEFERRED STAGE SCREEN
        deferredStageScreenVertexArray = glFactory.createVertexArray();
        deferredStageScreenVertexArray.setData(MeshGenerator.generateTexturedPlane(null, new Vector2f(2, 2)));
        deferredStageScreenVertexArray.create();
    }

    public static void dispose() {
        disposeEffects();
        disposePrograms();
        disposeTextures();
        disposeFrameBuffers();
        disposeVertexArrays();
        disposeContext();
    }

    private static void disposeContext() {
        // CONTEXT
        context.destroy();
    }

    private static void disposeEffects() {
        // SSAO
        ssaoEffect.dispose();
        // SHADOW MAPPING
        shadowMappingEffect.dispose();
        // BLUR
        blurEffect.dispose();
    }

    private static void disposePrograms() {
        for (Program program : programs.values()) {
            // SHADERS
            for (Shader shader : program.getShaders()) {
                shader.destroy();
            }
            // PROGRAM
            program.destroy();
        }
    }

    private static void disposeTextures() {
        // COLOR
        colorsTexture.destroy();
        // NORMALS
        normalsTexture.destroy();
        // VERTEX NORMALS
        vertexNormals.destroy();
        // MATERIALS
        materialsTexture.destroy();
        // VELOCITIES
        velocitiesTexture.destroy();
        // DEPTHS
        depthsTexture.destroy();
        // LIGHT DEPTHS
        lightDepthsTexture.destroy();
        // SSAO
        ssaoTexture.destroy();
        // SHADOW
        shadowTexture.destroy();
        // AUX R
        auxRTexture.destroy();
        // AUX RGB
        auxRGBATexture.destroy();
    }

    private static void disposeFrameBuffers() {
        // MODEL
        modelFrameBuffer.destroy();
        // SHADOW
        lightModelFrameBuffer.destroy();
        // SSAO
        ssaoFrameBuffer.destroy();
        // SHADOW
        shadowFrameBuffer.destroy();
        // BLUR
        blurFrameBuffer.destroy();
        // LIGHTING
        lightingFrameBuffer.destroy();
        // MOTION BLUR
        motionBlurFrameBuffer.destroy();
        // ANTI ALIASING
        antiAliasingFrameBuffer.destroy();
    }

    private static void disposeVertexArrays() {
        // DEFERRED STAGE SCREEN
        deferredStageScreenVertexArray.destroy();
    }

    public static void setGLVersion(GLVersion version) {
        glVersion = version;
        glFactory = GLImplementation.get(version);
    }

    public static void setCullBackFaces(boolean cull) {
        cullBackFaces = cull;
    }

    public static void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    public static void setLightAttenuation(float attenuation) {
        lightAttenuationUniform.set(attenuation);
    }

    public static void setSolidColor(Color color) {
        solidModelColor = color;
    }

    public static Camera getCamera() {
        return modelCamera;
    }

    public static void setLightPosition(Vector3f position) {
        lightPositionUniform.set(position);
        lightCamera.setPosition(position);
    }

    public static void setLightDirection(Vector3f direction) {
        direction = direction.normalize();
        spotDirectionUniform.set(direction);
        lightCamera.setRotation(Quaternionf.fromRotationTo(Vector3f.FORWARD.negate(), direction));
    }

    public static Model addSolid(VertexData vertexData, Vector3f position, Quaternionf orientation) {
        final VertexArray vertexArray = glFactory.createVertexArray();
        vertexArray.setData(vertexData);
        vertexArray.create();
        final Model model = new Model(vertexArray, solidMaterial);
        model.setPosition(position);
        model.setRotation(orientation);
        model.getUniforms().add(new ColorUniform("modelColor", solidModelColor));
        addModel(model);
        return model;
    }

    public static void addModel(Model model) {
        model.getUniforms().add(new Matrix4Uniform("previousModelMatrix", model.getMatrix()));
        modelRenderList.add(model);
    }

    public static void removeModel(Model model) {
        modelRenderList.remove(model);
    }

    public static void addDefaultObjects() {
        addScreen();
        addFPSMonitor();
    }

    private static void addScreen() {
        guiRenderList.add(new Model(deferredStageScreenVertexArray, screenMaterial));
    }

    private static void addFPSMonitor() {
        final Font ubuntu;
        try {
            ubuntu = Font.createFont(Font.TRUETYPE_FONT, Renderer.class.getResourceAsStream("/fonts/ubuntu-r.ttf"));
        } catch (FontFormatException | IOException e) {
            System.out.println(e);
            return;
        }
        final StringModel sandboxModel = new StringModel(glFactory, programs.get("font"), "ClientWIPFPS0123456789-: ", ubuntu.deriveFont(Font.PLAIN, 15), WINDOW_SIZE.getFloorX());
        final float aspect = 1 / ASPECT_RATIO;
        sandboxModel.setPosition(new Vector3f(0.005, aspect / 2 + 0.315, -0.1));
        sandboxModel.setString("Client - WIP");
        guiRenderList.add(sandboxModel);
        final StringModel fpsModel = sandboxModel.getInstance();
        fpsModel.setPosition(new Vector3f(0.005, aspect / 2 + 0.285, -0.1));
        fpsModel.setString("FPS: " + fpsMonitor.getTPS());
        guiRenderList.add(fpsModel);
        fpsMonitorModel = fpsModel;
    }

    public static void startFPSMonitor() {
        fpsMonitor.start();
    }

    public static void render() {
        // UPDATE PER-FRAME UNIFORMS
        inverseViewMatrixUniform.set(modelCamera.getViewMatrix().invert());
        lightViewMatrixUniform.set(lightCamera.getViewMatrix());
        lightProjectionMatrixUniform.set(lightCamera.getProjectionMatrix());
        blurStrengthUniform.set((float) fpsMonitor.getTPS() / Interface.TPS);
        // RENDER
        pipeline.run(context);
        // UPDATE PREVIOUS FRAME UNIFORMS
        setPreviousModelMatrices();
        previousViewMatrixUniform.set(modelCamera.getViewMatrix());
        previousProjectionMatrixUniform.set(modelCamera.getProjectionMatrix());
        // UPDATE FPS
        updateFPSMonitor();
    }

    private static void setPreviousModelMatrices() {
        for (Model model : modelRenderList) {
            model.getUniforms().getMatrix4("previousModelMatrix").set(model.getMatrix());
        }
    }

    private static void updateFPSMonitor() {
        fpsMonitor.update();
        fpsMonitorModel.setString("FPS: " + fpsMonitor.getTPS());
    }

    public static void saveScreenshot() {
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
            ImageIO.write(image, "PNG", new File(SCREENSHOT_DATE_FORMAT.format(Calendar.getInstance().getTime()) + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class DoDeferredStageAction extends RenderModelsAction {
        private final FrameBuffer frameBuffer;

        private DoDeferredStageAction(FrameBuffer frameBuffer, VertexArray screen, Material material) {
            super(Arrays.asList(new Model(screen, material)));
            this.frameBuffer = frameBuffer;
        }

        @Override
        public void execute(Context context) {
            frameBuffer.bind();
            super.execute(context);
        }
    }
}
