package net.vitacraft.api.classloader;

import net.vitacraft.exceptions.CircularDependencyException;

import java.util.*;

/**
 * The {@code ModuleSorter} class provides a method to sort modules based on their dependencies.
 * <p>
 * This class provides a method to sort modules based on their dependencies using a topological sort algorithm.
 * The method takes a graph of module dependencies as input and returns a list of module names in the order
 * in which they should be enabled during the startup sequence.
 * </p>
 */
public class ModuleSorter {

    /**
     * Sorts the modules based on their dependencies using a topological sort algorithm.
     *
     * @param graph the graph representing the module dependencies
     * @return a list of module names in the order in which they should be enabled
     * @throws CircularDependencyException if a circular dependency is detected in the graph
     */
    public static List<String> topologicalSort(Map<String, Set<String>> graph) throws CircularDependencyException {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        Stack<String> stack = new Stack<>();
        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                topologicalSortUtil(node, visited, recursionStack, stack, graph);
            }
        }
        List<String> sortedList = new ArrayList<>();
        while (!stack.isEmpty()) {
            sortedList.add(stack.pop());
        }
        return sortedList;
    }

    /**
     * Utility method to perform the topological sort of the modules.
     *
     * @param node          the current node being visited
     * @param visited       the set of visited nodes
     * @param recursionStack the set of nodes in the recursion stack
     * @param stack         the stack to store the sorted nodes
     * @param graph         the graph representing the module dependencies
     * @throws CircularDependencyException if a circular dependency is detected in the graph
     */
    private static void topologicalSortUtil(String node, Set<String> visited, Set<String> recursionStack, Stack<String> stack, Map<String, Set<String>> graph) throws CircularDependencyException {
        visited.add(node);
        recursionStack.add(node);
        for (String neighbor : graph.get(node)) {
            if (!visited.contains(neighbor)) {
                topologicalSortUtil(neighbor, visited, recursionStack, stack, graph);
            } else if (recursionStack.contains(neighbor)) {
                throw new CircularDependencyException("Circular dependency detected: " + node + " <-> " + neighbor);
            }
        }
        recursionStack.remove(node);
        stack.push(node);
    }
}