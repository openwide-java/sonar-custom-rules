@Entity
@Inject
class AvoidFinalNonStaticMethodInEntityTestClass {
	AvoidFinalNonStaticMethodInEntityTestClass(AvoidFinalNonStaticMethodInEntityTestClass a) {
	}

	final void foo10() { // Noncompliant
	}

	int foo1() {
		return 0;
	}

	void foo2(int value) {
	}

	int foo3(int value) {
		return 0;
	}

	Object foo4(int value) {
		return null;
	}

	AvoidFinalNonStaticMethodInEntityTestClass foo5(AvoidFinalNonStaticMethodInEntityTestClass avalue) {
		return null;
	}

	int foo6(int value, String name) {
		return 0;
	}

	int foo7(int... values) {
		return 0;
	}
}