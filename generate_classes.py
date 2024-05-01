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

# These are left over from the C# version.

define_ast("Expr", [
    "Assign      : Token name, Token operator, Expr value",
    "Attr        : Expr expr, Token name",
    "Binary      : Expr left, Token operator, Expr right",
    "Call        : Expr callee, List<Expr> arguments",
    "Dict        : List<Expr> keys, List<Expr> values",
    "Grouping    : Expr expression",
    "Index       : Expr expr, Expr index",
    "ListLiteral : List<Expr> elements",
    "Literal     : Object value",
    "Logical     : Expr left, Token operator_, Expr right",
    "Unary       : Token operator, Expr right",
    "Var         : Token name"
])

define_ast("Stmt", [
    "Block      : List<Stmt> statements",
    "Break      : Token keyword, Expr value",
    "Class      : Token name, List<Token> inherits, List<Stmt> methods",
    "Expression : Expr expression",
    "Foreach    : Token loopVar, Expr iter, Stmt body",
    "Function   : Token name, Map<Token,Object> parameters, Stmt body",
    "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
    "Import     : Token name, Token from",
    "Method     : Token name, Map<Token,Object> parameters, Stmt body",
    "Print      : Expr expression",
    "Repeat     : Expr amount, Stmt body",
    "Return     : Token keyword, Expr value",
    "Var        : Token name, Expr initializer",
    "While      : Expr condition, Stmt body"
])