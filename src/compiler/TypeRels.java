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

		if (a instanceof RefTypeNode at && b instanceof RefTypeNode bt)
			return checkSuperType(at.id, bt.id);

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
		return a != null &&
				b != null &&
//				first check if the ref. types refer to the same class, otherwise check if the class is a subtype
				(a.equals(b) || (Objects.equals(superType.get(a), b) || checkSuperType(superType.get(a), b)));
	}

	public static TypeNode lowestCommonAncestor(TypeNode a, TypeNode b) {

		if (a instanceof EmptyTypeNode && b instanceof RefTypeNode) return b;

		if (b instanceof EmptyTypeNode && a instanceof RefTypeNode) return a;

		if (a instanceof RefTypeNode at && b instanceof RefTypeNode) {

			var superClassId = at.id;

			while (!superClassId.isEmpty()) {
				var superClassType = new RefTypeNode(superClassId);
				if (isSubtype(b, superClassType)) return superClassType;
				superClassId = superType.get(superClassId) == null ? "" : superType.get(superClassId);
			}
		}

		if (isSubtype(a, new IntTypeNode()) && isSubtype(b, new IntTypeNode()))
			return (a instanceof IntTypeNode || b instanceof IntTypeNode) ? new IntTypeNode() : new BoolTypeNode();

		return null;
	}

}
