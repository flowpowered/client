package org.spoutcraft.client.nterface.render.graph.node;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.spout.renderer.api.Creatable;
import org.spout.renderer.api.gl.Texture;

import org.spoutcraft.client.nterface.render.graph.RenderGraph;

/**
 *
 */
public abstract class GraphNode extends Creatable {
    protected final RenderGraph graph;
    protected final String name;
    protected final Map<String, Method> inputs = new HashMap<>();
    protected final Map<String, Method> outputs = new HashMap<>();
    protected final Map<String, GraphNode> inputNodes = new HashMap<>();
    protected final Map<String, GraphNode> outputNodes = new HashMap<>();

    protected GraphNode(RenderGraph graph, String name) {
        this.graph = graph;
        this.name = name;
        findInputsAndOutputs();
    }

    public abstract void render();

    public String getName() {
        return name;
    }

    public Set<String> getInputs() {
        return Collections.unmodifiableSet(inputs.keySet());
    }

    public Set<String> getOutputs() {
        return Collections.unmodifiableSet(outputs.keySet());
    }

    public Map<String, GraphNode> getConnectedInputs() {
        return Collections.unmodifiableMap(inputNodes);
    }

    public Map<String, GraphNode> getConnectedOutputs() {
        return Collections.unmodifiableMap(outputNodes);
    }

    public void connect(String input, String output, GraphNode parent) {
        setInput(input, parent.getOutput(output));
        inputNodes.put(input, parent);
        parent.outputNodes.put(output, this);
    }

    private void setInput(String name, Object input) {
        try {
            inputs.get(name).invoke(this, input);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to set node input", ex);
        }
    }

    private Object getOutput(String name) {
        try {
            return outputs.get(name).invoke(this);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get node output", ex);
        }
    }

    private void findInputsAndOutputs() {
        for (Method method : getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            final Input inputAnnotation = method.getAnnotation(Input.class);
            final Output outputAnnotation = method.getAnnotation(Output.class);
            if (inputAnnotation != null) {
                if (outputAnnotation != null) {
                    throw new IllegalStateException("Input and output annotations cannot be both present");
                }
                final Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1 || !Texture.class.isAssignableFrom(parameterTypes[0])) {
                    throw new IllegalStateException("Output method must have one argument of type org.spout.renderer.api.gl.Texture");
                }
                inputs.put(inputAnnotation.value(), method);
            } else if (outputAnnotation != null) {
                if (!Texture.class.isAssignableFrom(method.getReturnType())) {
                    throw new IllegalStateException("Input method must have return type org.spout.renderer.api.gl.Texture");
                }
                outputs.put(outputAnnotation.value(), method);
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Input {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Output {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Setting {
    }
}
