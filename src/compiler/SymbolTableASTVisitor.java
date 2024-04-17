package compiler;

import java.util.*;
import compiler.AST.*;
import compiler.exc.*;
import compiler.lib.*;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void,VoidException> {
	
	private final List<Map<String, STentry>> symTable = new ArrayList<>();
	private final Map<String,Map<String, STentry>> classTable = new HashMap<>();
	private int nestingLevel=0; // current nesting level
	private int decOffset=-2; // counter for offset of local declarations at current nesting level 
	int stErrors=0;

	SymbolTableASTVisitor() {}
	SymbolTableASTVisitor(boolean debug) {super(debug);} // enables print for debugging

	private STentry stLookup(String id) {
		int j = nestingLevel;
		STentry entry = null;
		while (j >= 0 && entry == null) 
			entry = symTable.get(j--).get(id);	
		return entry;
	}

	@Override
	public Void visitNode(ProgLetInNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = new HashMap<>();
		symTable.add(hm);
	    for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		symTable.remove(0);
		return null;
	}

	@Override
	public Void visitNode(ProgNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}
	
	@Override
	public Void visitNode(FunNode n) {
		if (print) printNode(n);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		List<TypeNode> parTypes = new ArrayList<>();  
		for (ParNode par : n.parlist) parTypes.add(par.getType()); 
		STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes,n.retType),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		} 
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level 
		decOffset=-2;
		
		int parOffset=1;
		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope               
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level 
		return null;
	}
	
	@Override
	public Void visitNode(VarNode n) {
		if (print) printNode(n);
		visit(n.exp);
		Map<String, STentry> hm = symTable.get(nestingLevel);
		STentry entry = new STentry(nestingLevel,n.getType(),decOffset--);
		//inserimento di ID nella symtable
		if (hm.put(n.id, entry) != null) {
			System.out.println("Var id " + n.id + " at line "+ n.getLine() +" already declared");
			stErrors++;
		}
		return null;
	}

	@Override
	public Void visitNode(PrintNode n) {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(IfNode n) {
		if (print) printNode(n);
		visit(n.cond);
		visit(n.th);
		visit(n.el);
		return null;
	}
	
	@Override
	public Void visitNode(EqualNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(TimesNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}
	
	@Override
	public Void visitNode(PlusNode n) {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(CallNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Fun id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		for (Node arg : n.arglist) visit(arg);
		return null;
	}

	@Override
	public Void visitNode(IdNode n) {
		if (print) printNode(n);
		STentry entry = stLookup(n.id);
		if (entry == null) {
			System.out.println("Var or Par id " + n.id + " at line "+ n.getLine() + " not declared");
			stErrors++;
		} else {
			n.entry = entry;
			n.nl = nestingLevel;
		}
		return null;
	}

	@Override
	public Void visitNode(BoolNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	@Override
	public Void visitNode(IntNode n) {
		if (print) printNode(n, n.val.toString());
		return null;
	}

	// FOOL EXTENSION

	@Override
	public Void visitNode(OrNode n) throws VoidException {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(AndNode n) throws VoidException {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(MinusNode n) throws VoidException {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(DivNode n) throws VoidException {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(NotNode n) throws VoidException {
		if (print) printNode(n);
		visit(n.exp);
		return null;
	}

	@Override
	public Void visitNode(GreaterEqualNode n) throws VoidException {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	@Override
	public Void visitNode(LessEqualNode n) throws VoidException {
		if (print) printNode(n);
		visit(n.left);
		visit(n.right);
		return null;
	}

	// OOP EXTENSION

	@Override
	public Void visitNode(ClassNode n) throws VoidException {
		if (print) printNode(n);
		if (nestingLevel != 0){
			System.out.println("Class " + n.id + " declared at line " + n.getLine() + ", not in the first scope");
			stErrors++;
		}

		// optimization: used for check if something is already declared inside the class
		HashSet<String> declarations = new HashSet<>();

		// I need to update thees with the methods and the fields declared inside the class
		// set values to avoid null pointer exceptions in case of symbol table errors
		ClassTypeNode classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
		Map<String, STentry> virtualTable = new HashMap<>();
		// Add class map id into the symmbol table at level 0
		Map<String, STentry> hm = symTable.get(0);

		//no inheritance
		if (n.superID == null) {
			classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
			virtualTable = new HashMap<>();
		} else { //inheritance
			if (!classTable.containsKey(n.superID)) {
				System.out.println("SuperClass id " + n.superID + " at line " + n.getLine() + " after extends declaration doesn't exist");
				stErrors++;
			} else {
				n.superEntry = hm.get(n.superID);
				var superClassType = (ClassTypeNode) n.superEntry.type;
				classType = new ClassTypeNode(new ArrayList<>(superClassType.allFields), new ArrayList<>(superClassType.allMethods));
				virtualTable = new HashMap<>(classTable.get(n.superID));
			}
		}

		if (hm.put(n.id, new STentry(0, classType, decOffset--)) != null) {
			System.out.println("Class id " + n.id + " at line " + n.getLine() + " already declared");
			stErrors++;
		} else {// Add class map with id into the class table
			if (classTable.put(n.id, virtualTable) != null) {
				System.out.println("Class id " + n.id + " at line " + n.getLine() + " already in the class table");
				stErrors++;
			}
		}

		symTable.add(virtualTable);
		// inside class declaration
		nestingLevel++;

		//visit fields

		//field offset is -1 for the chosen layout
		int fieldOffset = -classType.allFields.size()-1;

		for (FieldNode field : n.fields) {

			if (declarations.contains(field.id)) { //optimization
				System.out.println("Field id " + field.id + " at line " + n.getLine() + " already declared inside class "+n.id);
				stErrors++;
			} else {
				declarations.add(field.id);
				if (virtualTable.containsKey(field.id)) { //overriding field
					if (virtualTable.get(field.id).type instanceof MethodTypeNode) {
						System.out.println("Overriding field id " + field.id + " at line " + n.getLine() + " is not a field in superclass");
						stErrors++;
					} else {
						int prevOffset = virtualTable.get(field.id).offset;
						STentry entry = new STentry(nestingLevel, field.getType(), prevOffset);
						virtualTable.put(field.id, entry);
						classType.allFields.set(-prevOffset-1, field.getType());
					}
				} else {//new field
					STentry entry = new STentry(nestingLevel, field.getType(), fieldOffset--);
					virtualTable.put(field.id, entry);
					classType.allFields.add(field.getType());
				}
			}
		}

		int prevNLDecOffset=decOffset;
		decOffset = classType.allMethods.size();

		//visit methods
		for (MethodNode method : n.methods) {

			if (declarations.contains(method.id)) { //optimization
				System.out.println("Method id " + method.id + " at line " + n.getLine() + " already declared inside class " + n.id);
				stErrors++;
			} else {
				declarations.add(method.id);
				visit(method);
				if (virtualTable.containsKey(method.id)) {
					if (!(virtualTable.get(method.id).type instanceof MethodTypeNode)) {
						System.out.println("Overriding method id " + method.id + " at line " + n.getLine() + " is not a method in superclass");
						stErrors++;
					} else { //override
						int methodOffset = virtualTable.get(method.id).offset;
						STentry entry = new STentry(nestingLevel, method.getType(), methodOffset);
						method.offset = methodOffset;
						virtualTable.put(method.id, entry);
						classType.allMethods.set(method.offset, ((MethodTypeNode) method.getType()).fun);
					}
				} else { //new method
					STentry entry = new STentry(nestingLevel, method.getType(), decOffset);
					//inserimento di ID nella symtable
					if (virtualTable.put(method.id, entry) != null) {
						System.out.println("Method id " + method.id + " at line "+ n.getLine() +" already declared");
						stErrors++;
					}
					method.offset = decOffset++;
					classType.allMethods.add(((MethodTypeNode) method.getType()).fun);
				}
			}
		}
		n.typeNode = classType;

		symTable.remove(nestingLevel--);
		decOffset = prevNLDecOffset;

		return null;
	}

	@Override
	public Void visitNode(MethodNode n) throws VoidException {
		if (print) printNode(n);
		List<TypeNode> parTypes = new ArrayList<>();
		for (ParNode par : n.parlist) parTypes.add(par.getType());
		n.setType(new MethodTypeNode(new ArrowTypeNode(parTypes,n.retType)));
		//creare una nuova hashmap per la symTable
		nestingLevel++;
		Map<String, STentry> hmn = new HashMap<>();
		symTable.add(hmn);
		int prevNLDecOffset=decOffset; // stores counter for offset of declarations at previous nesting level
		int parOffset=1;

		for (ParNode par : n.parlist)
			if (hmn.put(par.id, new STentry(nestingLevel,par.getType(),parOffset++)) != null) {
				System.out.println("Par id " + par.id + " at line "+ n.getLine() +" already declared");
				stErrors++;
			}
		for (Node dec : n.declist) visit(dec);
		visit(n.exp);
		//rimuovere la hashmap corrente poiche' esco dallo scope
		symTable.remove(nestingLevel--);
		decOffset=prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
		return null;
	}

	@Override
	public Void visitNode(ClassCallNode n) throws VoidException {
		if (print) printNode(n);
		STentry objectEntry = stLookup(n.objectId);

		if (objectEntry == null) {
			System.out.println("Object id " + n.objectId + " at line "+ n.getLine() +" used but not declared");
			stErrors++;
		} else {
			if (objectEntry.type instanceof RefTypeNode) {
				String objectClassId = ((RefTypeNode) objectEntry.type).id;
				STentry methodEntry = classTable.get(objectClassId).get(n.methodId);

				if (methodEntry == null) {
					System.out.println("Method id " + n.objectId + " at line "+ n.getLine() +" used but not declared");
					stErrors++;
				} else {
					n.entry = objectEntry;
					n.methodEntry = methodEntry;
					n.nl = nestingLevel;
				}
			} else {
				System.out.println("Object id " + n.objectId + " at line " + n.getLine() + " is not a class");
				stErrors++;
			}
		}

		for (Node arg : n.argList) visit(arg);

		return null;
	}

	@Override
	public Void visitNode(NewNode n) throws VoidException {
		if (print) printNode(n);

		if (!classTable.containsKey(n.id)) {
			System.out.println("Class " + n.id + " at line " + n.getLine() + " not declared");
			stErrors++;
		} else {
			STentry entry = symTable.get(0).get(n.id);
			if (entry == null) {
				System.out.println("Class " + n.id + " at line " + n.getLine() + " not declared at level 0");
				stErrors++;
			} else n.entry = entry;
		}

		for (Node arg : n.argList) visit(arg);

		return null;
	}

	@Override
	public Void visitNode(EmptyNode n) throws VoidException {
		if (print) printNode(n);
		return null;
	}
}
