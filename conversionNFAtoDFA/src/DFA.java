import java.util.*;

public class DFA {
    Set<Set<Integer>> states;
    Set<String> alphabet;
    Map<String, Map<Set<Integer>, Set<Integer>>> transitions;
    Set<Set<Integer>> initialStates;
    Set<Set<Integer>> finalStates;

    public DFA(Set<Set<Integer>> states, Set<String> alphabet, Map<String, Map<Set<Integer>, Set<Integer>>> transitions,
               Set<Set<Integer>> initialStates, Set<Set<Integer>> finalStates) {
        this.states = states;
        this.alphabet = alphabet;
        this.transitions = transitions;
        this.initialStates = initialStates;
        this.finalStates = finalStates;
    }
}
