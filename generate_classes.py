from pathlib import Path

def define_type(base_name, class_name, field_list):
    fields = field_list.split(', ')
    params = field_defs = ''
    if fields[0]:
        params = '\n            '.join([f'this.{f} = {f};' for f in [f.split(' ')[1] for f in fields]])
        field_defs = '\n        '.join([f'public final {f};' for f in fields])
    return '\n    '+f'''

    static class {class_name} extends {base_name} {{
        {class_name}({field_list}) {{
            {params}
        }}

        @Override
        <T> T accept(Visitor<T> visitor) {{
            return visitor.visit(this);
        }}

        {field_defs}
    }}

    '''.strip()+'\n'

def define_visitor(base_name, types):
    res = '\n    public interface Visitor<T> {'
    for type in types:
        type_name = type.split(':')[0].strip()
        res += f'\n        T visit({type_name} {base_name.lower()});'
    res += '\n    }\n'
    return res

def define_ast(base_name, types):
    res = 'package com.esjr.bluejay;\n\nimport java.util.*;\n\n'
    res += f'abstract class {base_name} {{'

    res += define_visitor(base_name, types)

    for type in types:
        class_name = type.split(':')[0].strip()
        fields = type.split(':')[1].strip()
        res += define_type(base_name, class_name, fields)

    res += '\n    abstract <T> T accept(Visitor<T> visitor);'

    res += '\n}'
    with open(f'src/com/esjr/bluejay/{base_name}.java', 'w') as f:
        f.write(res)

def define_printer(classes):
    code = '''package com.esjr.bluejay;

import java.util.*;
import java.util.stream.Collectors;

class AstPrinter implements '''+', '.join(c[0]+'.Visitor<String>' for c in classes)+' {\n    public String visit(Object thing) {\n        if (thing instanceof Expr) return visit((Expr)thing);\n        if (thing instanceof Stmt) return visit((Stmt)thing);\n        return thing.toString();\n    }\n\n    public String visit(List thing) {\n        List<String> elems = new ArrayList<String>();\n        for (Object e : thing) {\n            elems.add(visit(e));\n        }\n        return "["+String.join(", ", elems)+"]";\n    }\n\n'
    mthds = []
    for c in classes:
        mthds.append('    public String visit('+c[0]+' thing) {\n        '+'\n        '.join([
            'if (thing instanceof '+c[0]+'.'+s.split(':')[0].strip()+') return visit(('+c[0]+'.'+s.split(':')[0].strip()+')thing);' for s in c[1]
        ])+'\n        return null;\n    }')
        for s in c[1]:
            name = s.split(':')[0].strip()
            atts = [a.strip().split(' ')[1].strip() for a in s.split(':')[1].split(', ')]
            mthds.append('    public String visit('+c[0]+'.'+name+' thing) {\n        return "'+c[0]+'.'+name+': '+'" + '+' + '.join('"'+a.upper()+'(" + visit(thing.'+a+') + "), "' for a in atts)[:-3]+';";\n    }')
    code += '\n\n'.join(mthds)+'\n}'
    with open('src/com/esjr/bluejay/AstPrinter.java','w') as f:
        f.write(code)

expr = ("Expr", [
    "Assign      : Token name, Token operator, Expr value",
    "Get         : Expr expr, Token name",
    "Set         : Expr expr, Token name, Token operator, Expr value",
    "Binary      : Expr left, Token operator, Expr right",
    "Call        : Expr callee, Token paren, List<Expr> arguments",
    "Dict        : List<Expr> keys, List<Expr> values",
    "Grouping    : Expr expression",
    "Index       : Expr expr, Expr index",
    "ListLiteral : List<Expr> elements",
    "Literal     : Value value",
    "Logical     : Expr left, Token operator, Expr right",
    "Unary       : Token operator, Expr right",
    "Var         : Token name"
])

stmt = ("Stmt", [
    "Block      : List<Stmt> statements",
    "Break      : Token keyword, Expr value",
    "Class      : Token name, Expr.Var inherits, List<Stmt> methods",
    "Expression : Expr expression",
    "Foreach    : Token loopVar, Expr iter, Stmt body",
    "Function   : Token name, LinkedHashMap<Token,Object> parameters, Stmt body",
    "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
    "Import     : Token name, Token from",
    "Method     : Token name, LinkedHashMap<Token,Object> parameters, Stmt body",
    "Print      : Expr expression",
    "Repeat     : Token paren, Expr amount, Stmt body",
    "Return     : Token keyword, Expr value",
    "Var        : Token name, Expr initializer",
    "While      : Expr condition, Stmt body"
])

define_ast(*expr)
define_ast(*stmt)
define_printer([expr,stmt])