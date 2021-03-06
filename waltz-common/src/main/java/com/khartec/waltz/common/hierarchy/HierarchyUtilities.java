/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.common.hierarchy;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.ListUtilities;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;


public class HierarchyUtilities {


    /**
     * Given a set of flat nodes, will construct a hierarchy and
     * return a tuple of a map of all nodes, and the collection
     * of root nodes.
     *
     * @param flatNodes
     * @param <T, K>
     * @return
     */
    public static <T, K> Forest<T, K> toForest(Collection<FlatNode<T, K>> flatNodes) {
        List<K> rootNodeIds = flatNodes.stream()
                .filter(n -> !n.getParentId().isPresent())
                .map(n -> n.getId())
                .collect(Collectors.toList());

        Map<K, Node<T, K>> allById = flatNodes.stream()
                .collect(toMap(
                        n -> n.getId(),
                        n -> new Node<>(n.getId(), n.getData()),
                        (n1, n2) -> n1));

        flatNodes.stream()
                .filter(n -> n.getParentId().isPresent())
                .forEach(n -> {
                    Node<T, K> parent = allById.get(n.getParentId().get());
                    if (parent == null) {
                        // no parent, therefore must be a root which we aren't interested in
                        return;
                    }
                    Node<T, K> node = allById.get(n.getId());
                    parent.addChild(node);
                    node.setParent(parent);
                });

        Set<Node<T, K>> rootNodes = rootNodeIds.stream()
                .map(id -> allById.get(id))
                .collect(Collectors.toSet());

        return new Forest(allById, rootNodes);
    }


    public static <T, K> boolean hasCycle(Forest<T, K> forest) {
        Checks.checkNotNull(forest, "forest must not be null");
        PMap<K, Node<T, K>> seen = HashTreePMap.empty();

        return forest.getAllNodes()
                .values()
                .stream()
                .anyMatch(node -> hasCycle(node, seen));
    }


    private static <T, K> boolean hasCycle(Node<T, K> node, PMap<K, Node<T, K>> seen) {
        if (seen.containsKey(node.getId())) {
            return true;
        }

        PMap<K, Node<T, K>> updated = seen.plus(node.getId(), node);

        return node.getChildren()
                .stream()
                .anyMatch(child -> hasCycle(child, updated));
    }


    /**
     * Returns a list of parent nodes, immediate parents first
     * @param startNode
     * @param <T>
     * @param <K>
     * @return
     */
    public static <T, K> List<Node<T, K>> parents(Node<T, K> startNode) {
        List<Node<T, K>> parents = ListUtilities.newArrayList();

        Node<T,K> parent = startNode.getParent();
        while (parent != null) {
            parents.add(parent);
            parent = parent.getParent();
        }

        return parents;
    }

    public static <T,K> List<T> flatten(Node<T, K> node) {
        List<T> ts = new ArrayList<>(1 + node.getChildren().size());
        return flatten(node, ts);
    }

    private static <T, K> List<T> flatten(Node<T, K> node, List<T> acc) {
        acc.add(node.getData());
        for (Node<T, K> child : node.getChildren()) {
            flatten(child, acc);
        }
        return acc;
    }


    public static <T, K> Map<K, Integer> assignDepths(Forest<T, K> forest) {
        return assignDepths(forest.getRootNodes(), 1);
    }


    public static <T, K> Map<K, Integer> assignDepths(Collection<Node<T, K>> rootNodes) {
        return assignDepths(rootNodes, 1);
    }


    private static <T, K> Map<K, Integer> assignDepths(Collection<Node<T, K>> nodes, int level) {
        Map<K, Integer> result = new HashMap<>();

        for (Node<T, K> node : nodes) {
            result.put(node.getId(), level);
            result.putAll(assignDepths(node.getChildren(), level + 1));
        }

        return result;
    }
}
