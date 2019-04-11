package net.termer.twister.scripting;

import java.util.HashMap;

public interface VariableProvider {
	public void provide(String domain, HashMap<String, Object> vars);
}
