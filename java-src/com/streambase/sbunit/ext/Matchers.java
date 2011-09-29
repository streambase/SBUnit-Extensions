package com.streambase.sbunit.ext;

import com.streambase.sb.Tuple;

public class Matchers {
    private static final class AllMatcher implements TupleMatcher {
        private final TupleMatcher m;
        private final TupleMatcher[] matchers;

        private AllMatcher(TupleMatcher m, TupleMatcher[] matchers) {
            this.m = m;
            this.matchers = matchers;
        }

        @Override
        public boolean matches(Tuple a) {
            boolean res = m.matches(a);
            for (TupleMatcher m : matchers) {
                res = res && m.matches(a);
            }
            return res;
        }

        @Override
        public String describeExpected() {
            StringBuilder res = new StringBuilder();
            res.append("all of ");
            res.append(m.describeExpected());
            for (TupleMatcher m : matchers) {
                res.append(", ");
                res.append(m.describeExpected());
            }
            return res.toString();
        }
    }

    private static final class AnyMatcher implements TupleMatcher {
        private final TupleMatcher[] matchers;
        private final TupleMatcher m;

        private AnyMatcher(TupleMatcher m, TupleMatcher[] matchers) {
            this.matchers = matchers;
            this.m = m;
        }

        @Override
        public boolean matches(Tuple a) {
            boolean res = m.matches(a);
            for (TupleMatcher m : matchers) {
                res = res || m.matches(a);
            }
            return res;
        }

        @Override
        public String describeExpected() {
            StringBuilder res = new StringBuilder();
            res.append("any of ");
            res.append(m.describeExpected());
            for (TupleMatcher m : matchers) {
                res.append(", ");
                res.append(m.describeExpected());
            }
            return res.toString();
        }
    }
    
    private static final class NotMatcher implements TupleMatcher {
        private final TupleMatcher m;

        private NotMatcher(TupleMatcher m) {
            this.m = m;
        }

        @Override
        public boolean matches(Tuple a) {
            return !m.matches(a);
        }

        @Override
        public String describeExpected() {
            return "does not match " + m.describeExpected();
        }
    }

    public static TupleMatcher anyOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AnyMatcher(m, matchers);
    }
    
    public static TupleMatcher allOf(TupleMatcher m, TupleMatcher... matchers ) {
        return new AllMatcher(m, matchers);
    }
    
    public static TupleMatcher not(TupleMatcher m) {
        return new NotMatcher(m);
    }
}
