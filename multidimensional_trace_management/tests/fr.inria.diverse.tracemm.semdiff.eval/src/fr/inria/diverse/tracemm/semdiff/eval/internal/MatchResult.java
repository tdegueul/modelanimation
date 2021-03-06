package fr.inria.diverse.tracemm.semdiff.eval.internal;

public class MatchResult {

	private boolean matches = false;
	private boolean matchedWithoutErrors = true;
	private boolean inconclusive = false;

	public MatchResult() {
	}

	public MatchResult(boolean matches, boolean matchedWithoutErrors) {
		this.matches = matches;
		this.matchedWithoutErrors = matchedWithoutErrors;
	}

	public MatchResult(boolean matches, boolean matchedWithoutErrors,
			boolean inconclusive) {
		this.matches = matches;
		this.matchedWithoutErrors = matchedWithoutErrors;
		this.inconclusive = inconclusive;
	}

	public boolean matches() {
		return matches;
	}

	public boolean matchedWithoutErrors() {
		return matchedWithoutErrors;
	}

	public boolean matchingInconclusive() {
		return inconclusive;
	}

	public void setMatchingInconclusive() {
		this.inconclusive = true;
	}

	public void setMatches(boolean matches) {
		this.matches = matches;
	}

	public void setMatchedWithoutErrors(boolean matchedWithoutErrors) {
		this.matchedWithoutErrors = matchedWithoutErrors;
	}
}
