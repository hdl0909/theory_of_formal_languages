import java.util.*;

public class NFAtoDFA {
    public static DFA nfaToDfa(NFA nfa) {
        Map<String, Map<Set<Integer>, Set<Integer>>> dfaTransitions = new HashMap<>();
        Set<Set<Integer>> dfaStates = new HashSet<>();
        Set<Set<Integer>> dfaFinalStates = new HashSet<>();
        Set<Set<Integer>> dfaInitialStates = new HashSet<>();

        //начальне состояние ДКА
        Set<Integer> curInitialStateDFA = new HashSet<>(nfa.initialStates);
        dfaStates.add(curInitialStateDFA);
        dfaInitialStates.add(curInitialStateDFA);

        //Очередь состояний для обработки
        Queue<Set<Integer>> queue = new LinkedList<>();
        queue.add(curInitialStateDFA);

        while (!queue.isEmpty()) {
            Set<Integer> currentStateSet = queue.poll();
            for (String symbol : nfa.alphabet) {
                // получаем переходы для каждого символа из текущего множества
                Set<Integer> nextStateSet = new HashSet<>();
                for (Integer state : currentStateSet) {
                    if (nfa.transitions.containsKey(symbol) &&
                            nfa.transitions.get(symbol).containsKey(state)) {
                        nextStateSet.addAll(nfa.transitions.get(symbol).get(state));
                    }
                }

                if (!nextStateSet.isEmpty()) {
                    if (!dfaStates.contains(nextStateSet)) {
                        dfaStates.add(nextStateSet);
                        queue.add(nextStateSet);
                    }

                    //Добавляем переход в таблицу переходов ДКА
                    Map<Set<Integer>, Set<Integer>> transitionsForSymbol = dfaTransitions.get(symbol);

                    if (transitionsForSymbol == null) {
                        transitionsForSymbol = new HashMap<>();
                        dfaTransitions.put(symbol, transitionsForSymbol);
                    }

                    transitionsForSymbol.put(currentStateSet, nextStateSet);

                    // Проверяем, есть ли хотя бы одно финальное состояние состояние в nextStateSet
                    for  (Integer state : nextStateSet) {
                        if (nfa.finalStates.contains(state)) {
                            dfaFinalStates.add(nextStateSet);
                            break;
                        }
                    }
                }
            }
        }
        return new DFA(dfaStates, nfa.alphabet, dfaTransitions, dfaInitialStates, dfaFinalStates);
    }
}
