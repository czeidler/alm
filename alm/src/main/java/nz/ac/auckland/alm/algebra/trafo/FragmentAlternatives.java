/*
 * Copyright 2016.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.algebra.trafo;

import nz.ac.auckland.alm.IArea;
import nz.ac.auckland.alm.algebra.Fragment;

import java.util.*;


public class FragmentAlternatives<T> {
    static public class Result<T> {
        final public TrafoHistory trafoHistory;
        final public Fragment fragment;
        final public T classification;

        public Result(TrafoHistory trafoHistory, Fragment fragment, T classification) {
            this.trafoHistory = trafoHistory;
            this.fragment = fragment;
            this.classification = classification;
        }
    }


    final private IAlternativeClassifier<T> classifier;
    final private IGroupDetector groupDetector;
    final List<ITransformation> trafos = new ArrayList<ITransformation>();

    public FragmentAlternatives(IAlternativeClassifier<T> classifier, IGroupDetector groupDetector) {
        this.classifier = classifier;
        this.groupDetector = groupDetector;
    }

    public void addTrafo(ITransformation transformation) {
        this.trafos.add(transformation);
    }

    public List<ITransformation> getTrafos() {
        return trafos;
    }

    static private Fragment replaceFragment(Fragment root, FragmentRef fragmentRef, Fragment child) {
        if (fragmentRef.getLevelPositions().size() == 0)
            return child;
        Fragment parent = root;
        for (int i = 0; i < fragmentRef.getLevelPositions().size() - 1; i++)
            parent = (Fragment)parent.getItemAt(fragmentRef.getLevelPositions().get(i));
        int index = fragmentRef.getLevelPositions().get(fragmentRef.getLevelPositions().size() - 1);
        Fragment.Item item = parent.getRawItemAt(index);
        item.setItem(child);
        return root;
    }

    static private Fragment getFragment(Fragment root, FragmentRef fragmentRef) {
        Fragment child = root;
        for (Integer index : fragmentRef.getLevelPositions())
            child = (Fragment) child.getItemAt(index);
        return child;
    }

    private void addResult(List<Result<T>> results, Result<T> result) {
        if (classifier.objectiveValue(result.classification) >= IAlternativeClassifier.INVALID_OBJECTIVE)
            return;
        for (Result<T> existingResult : results) {
            // todo add better result?
            if (existingResult.fragment.isEquivalent(result.fragment))
                return;
        }
        results.add(result);
    }

    class OngoingTrafoComparator implements Comparator<OngoingTrafo<T>> {
        @Override
        public int compare(OngoingTrafo<T> ongoingTrafo, OngoingTrafo<T> t1) {
            int priorityComparison = -((Integer)ongoingTrafo.getSelector().getPriority()).compareTo(
                    t1.getSelector().getPriority());
            if (priorityComparison != 0)
                return priorityComparison;

            Double objectValue0 = classifier.objectiveValue(ongoingTrafo.getClassification());
            Double objectValue2 = classifier.objectiveValue(t1.getClassification());

            return -objectValue0.compareTo(objectValue2);
        }
    }

    private void spawnOngoing(List<OngoingTrafo<T>> ongoingTrafos, OngoingTrafo<T> newOngoingTrafo,
                              boolean groupDetection) {
        if (!groupDetection) {
            ongoingTrafos.add(newOngoingTrafo);
            return;
        }
        // calculate all group permutations
        List<OngoingTrafo<T>> groupPermutation = new ArrayList<OngoingTrafo<T>>();
        groupPermutation.add(newOngoingTrafo);
        for (FragmentRef ref : newOngoingTrafo.getFragmentRefs()) {
            int currentSize = groupPermutation.size();
            // only iterate to the current size so we can add elements on the fly
            for (int i = 0; i < currentSize; i++) {
                OngoingTrafo<T> current = groupPermutation.get(i);
                Fragment fragment = getFragment(current.getAlternative(), ref);

                List<Fragment> groups = groupDetector.detect(fragment);
                for (Fragment group : groups) {
                    Fragment fragmentClone = current.getAlternative().clone();
                    fragmentClone = replaceFragment(fragmentClone, ref, group);
                    OngoingTrafo<T> clone = new OngoingTrafo<T>(current.getTrafoHistory().clone(), fragmentClone,
                            current.getClassification(), current.getFragmentRefs(), current.getSelector().create());
                    groupPermutation.add(clone);
                }
            }
        }
        // if a group has been detected remove the original trafo
        if (groupPermutation.size() > 1)
            groupPermutation.remove(newOngoingTrafo);
        ongoingTrafos.addAll(groupPermutation);
    }

    public List<Result<T>> calculateAlternatives(Fragment fragment, IPermutationSelector selector, int maxResults,
                                                 long maxTimes) {
        T classification = classifier.classify(fragment, new TrafoHistory());
        OngoingTrafo<T> root = new OngoingTrafo<T>(new TrafoHistory(), fragment.clone(), classification,
                Collections.singletonList(new FragmentRef()), selector);
        List<OngoingTrafo<T>> ongoingTrafos = new ArrayList<OngoingTrafo<T>>();
        OngoingTrafoComparator ongoingTrafoComparator = new OngoingTrafoComparator();
        spawnOngoing(ongoingTrafos, root, true);
        List<Result<T>> results = new ArrayList<Result<T>>();
        long startTime = System.currentTimeMillis();
        while (ongoingTrafos.size() > 0 && results.size() < maxResults) {
            OngoingTrafo<T> current = ongoingTrafos.get(0);
            List<OngoingTrafo<T>> newResults = performTransformations(current);
            if (current.done())
                ongoingTrafos.remove(0);

            boolean needsSorting = false;
            for (OngoingTrafo<T> newResult : newResults) {
                Result<T> result = new Result<T>(newResult.getTrafoHistory(), newResult.getAlternative(),
                        newResult.getClassification());
                addResult(results, result);

                if (newResult.getFragmentRefs().size() > 0) {
                    spawnOngoing(ongoingTrafos, newResult, false);
                    needsSorting = true;
                }
            }
            if (System.currentTimeMillis() - startTime > maxTimes)
                break;
            if (needsSorting)
                Collections.sort(ongoingTrafos, ongoingTrafoComparator);
        }
        return results;
    }

    public List<OngoingTrafo<T>> performTransformations(OngoingTrafo<T> ongoingTrafo) {
        List<List<ITransformation>> permutations = ongoingTrafo.getSelector().next(ongoingTrafo);
        if (permutations == null)
            return Collections.emptyList();
        List<OngoingTrafo<T>> results = new ArrayList<OngoingTrafo<T>>();
        for (List<ITransformation> permutation : permutations) {
            applyTransformations(ongoingTrafo, permutation, results);
            int permutationValue = OngoingTrafo.getPermutationValue(permutation, trafos);
            ongoingTrafo.getHandledPermutations().add(permutationValue);
        }
        return results;
    }

    private void applyTransformations(OngoingTrafo<T> ongoingTrafo, List<ITransformation> transformations,
                                      List<OngoingTrafo<T>> results) {
        final Fragment alternative = ongoingTrafo.getAlternative();
        final List<FragmentRef> fragmentRefs = ongoingTrafo.getFragmentRefs();
        assert transformations.size() == fragmentRefs.size();
        // list of trafo results for each fragment ref
        List<List<ITransformation.Result>> trafoResults = new ArrayList<List<ITransformation.Result>>();
        for (int i = 0; i < fragmentRefs.size(); i++) {
            ITransformation trafo = transformations.get(i);
            if (trafo == null) {
                trafoResults.add(Collections.<ITransformation.Result>singletonList(null));
                continue;
            }
            final FragmentRef fragmentRef = fragmentRefs.get(i);
            final Fragment fragment = getFragment(alternative, fragmentRef);
            final List<ITransformation.Result> alternatives = trafo.transform(fragment);
            trafoResults.add(alternatives);
        }

        // build ongoing trafos
        List<List<ITransformation.Result>> trafoList = calculatePermutations(trafoResults);
        for (List<ITransformation.Result> trafo : trafoList) {
            Fragment newFragment = alternative.clone();
            TrafoHistory.Entry historyEntry = new TrafoHistory.Entry();
            for (int i = 0; i < trafo.size(); i++) {
                ITransformation.Result trafoResult = trafo.get(i);
                if (trafoResult == null)
                    continue;
                FragmentRef fragmentRef = fragmentRefs.get(i);
                newFragment = replaceFragment(newFragment, fragmentRef, trafoResult.fragment);
                historyEntry.add(fragmentRef, transformations.get(i), trafoResult);
            }
            TrafoHistory history = ongoingTrafo.getTrafoHistory();
            if (historyEntry.size() > 0) {
                history = history.clone();
                history.add(historyEntry);
            }

            T classification = classifier.classify(newFragment, history);
            List<FragmentRef> nextLevel = getNextLevel(newFragment, ongoingTrafo.getFragmentRefs());

            IPermutationSelector selector = ongoingTrafo.getSelector();
            results.add(new OngoingTrafo<T>(history, newFragment, classification, nextLevel,
                    selector.create()));
        }
    }

    private List<FragmentRef> getNextLevel(Fragment alternative, List<FragmentRef> currentRefs) {
        List<FragmentRef> nextLevel = new ArrayList<FragmentRef>();
        for (int i = 0; i < currentRefs.size(); i++) {
            FragmentRef fragmentRef = currentRefs.get(i);
            Fragment fragment = getFragment(alternative, fragmentRef);
            // collect next level children
            for (int a = 0; a < fragment.size(); a++) {
                IArea area = fragment.getItemAt(a);
                if (!(area instanceof Fragment))
                    continue;
                FragmentRef clone = fragmentRef.clone();
                clone.enterFragment(a);
                nextLevel.add(clone);
            }
        }
        return nextLevel;
    }

    private void getPermutations(List<List<ITransformation.Result>> list, List<List<ITransformation.Result>> result,
                                 int index, List<ITransformation.Result> ongoing) {
        if (ongoing.size() == list.size()) {
            result.add(ongoing);
            return;
        }

        List<ITransformation.Result> currentSource = list.get(index);
        for (int i = 0; i < currentSource.size(); i++) {
            List<ITransformation.Result> clone;
            if (currentSource.size() == 1)
                clone = ongoing;
            else
                clone = new ArrayList<ITransformation.Result>(ongoing);
            clone.add(currentSource.get(i));
            getPermutations(list, result, index + 1, clone);
        }
    }

    private List<List<ITransformation.Result>> calculatePermutations(List<List<ITransformation.Result>> input) {
        List<List<ITransformation.Result>> out = new ArrayList<List<ITransformation.Result>>();
        getPermutations(input, out, 0, new ArrayList<ITransformation.Result>());
        return out;
    }
}
