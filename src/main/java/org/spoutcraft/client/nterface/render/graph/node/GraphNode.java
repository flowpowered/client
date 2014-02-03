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

import org.spoutcraft.client.nterface.render.graph.RenderGraph;

/**
 *
 */
public abstract class GraphNode extends Creatable {
    protected final RenderGraph graph;
    protected final String name;
    protected Map<String, GraphNode> parents;
    protected Map<String, GraphNode> children;
    protected Map<String, Method> inputs = new HashMap<>();
    protected Map<String, Method> outputs = new HashMap<>();

    protected GraphNode(RenderGraph graph, String name) {
        this.graph = graph;
        this.name = name;
        findInputsAndOutputs();
    }

    public abstract void render();

    public Set<String> getInputs() {
        return Collections.unmodifiableSet(inputs.keySet());
    }

    public Set<String> getOutputs() {
        return Collections.unmodifiableSet(outputs.keySet());
    }

    protected Method getInput(String name) {
        return inputs.get(name);
    }

    protected void setInput(String name, Object input) {
        try {
            getInput(name).invoke(this, input);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to set node input", ex);
        }
    }

    protected Object getOutput(String name) {
        try {
            return outputs.get(name).invoke(this);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get node output", ex);
        }
    }

    public void linkToOutput(String input, String output, GraphNode parent) {
        setInput(input, parent.getOutput(output));
    }

    public void linkInputTo(String output, String input, GraphNode child) {
        child.setInput(input, getOutput(output));
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
                inputs.put(inputAnnotation.value(), method);
            } else if (outputAnnotation != null) {
                outputs.put(outputAnnotation.value(), method);
            }
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface Input {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public @interface Output {
        String value();
    }
}
