import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("Enter set of states: ");
        Set<Integer> states = new HashSet<>();
        for (String state : input.nextLine().split(" ")) {
            states.add(Integer.parseInt(state));
        }

        System.out.println("Enter the input alphabet: ");
        Set<String> alphabet = new HashSet<>();
        for (String symbol : input.nextLine().split(" ")) {
            alphabet.add(symbol);
        }

        Map<String, Map<Integer, Set<Integer>>> transitions = new HashMap<>();
        System.out.println("Enter state-transitions function (current state, input character, next state): ");
        String line;
        while (!(line = input.nextLine()).isEmpty()) {
            String[] parts = line.substring(1, line.length() - 1).split(", ");
            int currentState = Integer.parseInt(parts[0]);
            String symbol = parts[1];
            int nextState = Integer.parseInt(parts[2]);

            Map<Integer, Set<Integer>> transitionsForSymbol = transitions.computeIfAbsent(symbol, k -> new HashMap<>());
            transitionsForSymbol.computeIfAbsent(currentState, k -> new HashSet<>()).add(nextState);
        }

        System.out.println("Enter a set of initial states: ");
        Set<Integer> initialStates = new HashSet<>();
        for (String state : input.nextLine().split(" "))  {
            initialStates.add(Integer.parseInt(state));
        }

        System.out.println("Enter a set of final states: ");
        Set<Integer> finalStates = new HashSet<>();
        for (String state : input.nextLine().split(" ")) {
            finalStates.add(Integer.parseInt(state));
        }

        // Здесь предполагается, что класс NFA и метод NFAtoDFA реализованы
        NFA nfa = new NFA(states, alphabet, transitions, initialStates, finalStates);

        DFA dfa = NFAtoDFA.nfaToDfa(nfa);

        System.out.println("DFA:");
        System.out.println("Set of states: " + dfa.states);
        System.out.println("Input alphabet: " + dfa.alphabet);
        System.out.println("State-transitions function:");
        for (String symbol_tr : dfa.transitions.keySet()) {
            for (Set<Integer> currentStateSet : dfa.transitions.get(symbol_tr).keySet()) {
                Set<Integer> nextStateSet = dfa.transitions.get(symbol_tr).get(currentStateSet);
                System.out.println("D(" + currentStateSet + ", " + symbol_tr + ") = " + nextStateSet);
            }
        }
        System.out.println("Initial states: " + dfa.initialStates);
        System.out.println("Final states: " + dfa.finalStates);
    }
}
