package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypeRels {

	// map used for mapping classes with their superclass, if any
	public static Map<String, String> superType = new HashMap<>();

	// valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
	public static boolean isSubtype(TypeNode a, TypeNode b) {

		if (a instanceof RefTypeNode at && b instanceof RefTypeNode bt) {
			// first check if the ref type refer to the same class
			if (at.id.equals(bt.id)) return true;
			// otherwise check if the class is a subtype
			return checkSuperType(((RefTypeNode) a).id, ((RefTypeNode) b).id);
		}

		if (a instanceof ArrowTypeNode at && b instanceof ArrowTypeNode bt) {

			if (at.parlist.size() != bt.parlist.size()) return false;

			for (int i = 0; i < at.parlist.size(); i++) {
				if (!isSubtype(bt.parlist.get(i), at.parlist.get(i))) return false;
			}

			return isSubtype(at.ret, bt.ret);
		}

		return a.getClass().equals(b.getClass()) ||
				((a instanceof BoolTypeNode) && (b instanceof IntTypeNode)) ||
				((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode));
	}

	private static boolean checkSuperType(String a, String b) {
		return a != null && b != null && (Objects.equals(superType.get(a), b) || checkSuperType(superType.get(a), b));
	}

}
