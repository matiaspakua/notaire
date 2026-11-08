package com.licensis.notaire.application.usecase.workflow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.licensis.notaire.domain.workflow.WorkflowNode;
import com.licensis.notaire.domain.workflow.WorkflowNodeKind;
import com.licensis.notaire.domain.workflow.WorkflowTransition;

/**
 * Domain-typed workflow graph consistency checks (CU72 / issue #1770).
 */
@Service
public class WorkflowValidationService {

    public ValidationResult validate(List<WorkflowNode> nodes, List<WorkflowTransition> transitions) {
        List<String> errors = new ArrayList<>();

        if (nodes.isEmpty()) {
            errors.add("The workflow has no nodes.");
            return ValidationResult.invalid(errors);
        }

        long initialCount = nodes.stream().filter(n -> n.kind() == WorkflowNodeKind.INITIAL).count();
        if (initialCount == 0) {
            errors.add("The workflow must have exactly one initial node.");
        } else if (initialCount > 1) {
            errors.add("The workflow has more than one initial node (" + initialCount + ").");
        }

        long finalCount = nodes.stream().filter(n -> n.kind() == WorkflowNodeKind.FINAL).count();
        if (finalCount == 0) {
            errors.add("The workflow must have at least one final node.");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }

        Set<Integer> reachable = reachableFrom(
                nodes.stream()
                        .filter(n -> n.kind() == WorkflowNodeKind.INITIAL)
                        .findFirst()
                        .map(WorkflowNode::id)
                        .orElse(-1),
                transitions);

        Set<Integer> allIds = nodes.stream().map(WorkflowNode::id).collect(Collectors.toSet());
        Set<Integer> unreachable = new HashSet<>(allIds);
        unreachable.removeAll(reachable);

        if (!unreachable.isEmpty()) {
            String names = nodes.stream()
                    .filter(n -> unreachable.contains(n.id()))
                    .map(n -> n.statusManagementName() != null
                            ? n.statusManagementName()
                            : String.valueOf(n.id()))
                    .collect(Collectors.joining(", "));
            errors.add("Nodes unreachable from the initial node (isolated): " + names + ".");
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }

    private Set<Integer> reachableFrom(Integer startId, List<WorkflowTransition> transitions) {
        Map<Integer, List<Integer>> adjacency = transitions.stream()
                .collect(Collectors.groupingBy(
                        WorkflowTransition::originNodeId,
                        Collectors.mapping(WorkflowTransition::destinationNodeId, Collectors.toList())));

        Set<Integer> visited = new HashSet<>();
        dfs(startId, adjacency, visited);
        return visited;
    }

    private void dfs(Integer node, Map<Integer, List<Integer>> adjacency, Set<Integer> visited) {
        if (visited.contains(node)) {
            return;
        }
        visited.add(node);
        for (Integer neighbour : adjacency.getOrDefault(node, List.of())) {
            dfs(neighbour, adjacency, visited);
        }
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
