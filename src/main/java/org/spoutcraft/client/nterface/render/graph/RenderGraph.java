package org.spoutcraft.client.nterface.render.graph;

import java.util.HashMap;
import java.util.Map;

import org.spoutcraft.client.nterface.render.graph.node.GraphNode;

/**
 *
 */
public class RenderGraph {
    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final Map<String, GraphNode> modelInputNodes = new HashMap<>();
}
