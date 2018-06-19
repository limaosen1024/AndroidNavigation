package me.listenzz.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Listen on 2018/1/11.
 */

public class NavigationFragment extends AwesomeFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.nav_fragment_navigation, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null) {
            if (rootFragment == null) {
                throw new IllegalArgumentException("必须通过 `setRootFragment` 指定 rootFragment");
            } else {
                performSetRootFragment(rootFragment);
            }
        }
    }

    @Override
    public boolean isParentFragment() {
        return true;
    }

    @Override
    protected AwesomeFragment childFragmentForAppearance() {
        return getTopFragment();
    }

    @Override
    protected boolean onBackPressed() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 1) {
            AwesomeFragment topFragment = getTopFragment();
            if (topFragment.backInteractive()) {
                popFragment();
            }
            return true;
        } else {
            return super.onBackPressed();
        }
    }

    private AwesomeFragment rootFragment;

    public void setRootFragment(@NonNull final AwesomeFragment fragment) {
        if (isAdded()) {
            throw new IllegalStateException("NavigationFragment 已经出于 added 状态，不可以再设置 rootFragment");
        }
        this.rootFragment = fragment;
    }

    public AwesomeFragment getRootFragment() {
        FragmentManager fragmentManager = getChildFragmentManager();
        int count = fragmentManager.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt(0);
            return (AwesomeFragment) fragmentManager.findFragmentByTag(backStackEntry.getName());
        }
        return null;
    }

    private void performSetRootFragment(AwesomeFragment fragment) {
       FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, PresentAnimation.None);
    }

    public void pushFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executePushFragment(fragment);
            }
        });
    }

    private void executePushFragment(AwesomeFragment fragment) {
        FragmentHelper.addFragmentToBackStack(getChildFragmentManager(), R.id.navigation_content, fragment, PresentAnimation.Push);
    }

    public void popToFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executePopToFragment(fragment);
            }
        });
    }

    private void executePopToFragment(AwesomeFragment fragment) {
        AwesomeFragment topFragment = getTopFragment();
        if (topFragment == fragment) {
            return;
        }
        topFragment.setAnimation(PresentAnimation.Push);
        fragment.setAnimation(PresentAnimation.Push);

        fragment.onFragmentResult(topFragment.getRequestCode(), topFragment.getResultCode(), topFragment.getResultData());
        getChildFragmentManager().popBackStack(fragment.getSceneId(), 0);
    }

    public void popFragment() {
        AwesomeFragment after = FragmentHelper.getLatterFragment(getChildFragmentManager(), getTopFragment());
        if (after != null) {
            popToFragment(this);
            return;
        }

        AwesomeFragment before = FragmentHelper.getAheadFragment(getChildFragmentManager(), getTopFragment());
        if (before != null) {
            popToFragment(before);
        }
    }

    public void popToRootFragment() {
        AwesomeFragment awesomeFragment = getRootFragment();
        if (awesomeFragment != null) {
            popToFragment(getRootFragment());
        }
    }

    public void replaceFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executeReplaceFragment(fragment);
            }
        });
    }

    private void executeReplaceFragment(AwesomeFragment fragment) {

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.executePendingTransactions();

        AwesomeFragment topFragment = getTopFragment();
        AwesomeFragment aheadFragment = FragmentHelper.getAheadFragment(fragmentManager, topFragment);
        topFragment.setAnimation(PresentAnimation.Fade);
        if (aheadFragment != null) {
            aheadFragment.setAnimation(PresentAnimation.Fade);
        }
        fragmentManager.popBackStack();
        Log.d(TAG, "pop fragment");

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (aheadFragment != null) {
            transaction.hide(aheadFragment);
        }
        fragment.setAnimation(PresentAnimation.Fade);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public void replaceToRootFragment(final AwesomeFragment fragment) {
        scheduleTaskAtStarted(new Runnable() {
            @Override
            public void run() {
                executeReplaceRootFragment(fragment);
            }
        });
    }

    private void executeReplaceRootFragment(AwesomeFragment fragment) {
        AwesomeFragment topFragment = getTopFragment();
        AwesomeFragment rootFragment = getRootFragment();
        topFragment.setAnimation(PresentAnimation.Fade);
        rootFragment.setAnimation(PresentAnimation.Fade);

        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.executePendingTransactions();
        fragmentManager.popBackStack(rootFragment.getSceneId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        Log.d(TAG, "pop fragment");

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setReorderingAllowed(true);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragment.setAnimation(PresentAnimation.Fade);
        transaction.add(R.id.navigation_content, fragment, fragment.getSceneId());
        transaction.addToBackStack(fragment.getSceneId());
        transaction.commit();
    }

    public void setChildFragments(List<AwesomeFragment> fragments) {
        // TODO
        // 弹出所有旧的 fragment

        // 添加所有新的 fragment
    }

    public AwesomeFragment getTopFragment() {
        if (isAdded()) {
            return (AwesomeFragment) getChildFragmentManager().findFragmentById(R.id.navigation_content);
        } else {
            return null;
        }
    }

    public NavigationFragment getNavigationFragment() {
        AwesomeFragment parent = getParentAwesomeFragment();
        if (parent != null) {
            NavigationFragment another = parent.getNavigationFragment();
            if (another != null && another != this) {
                throw new IllegalStateException("should not nest NavigationFragment");
            }
        }
        return this;
    }

}
