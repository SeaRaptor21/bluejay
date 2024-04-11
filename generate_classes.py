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

    res += '\n    abstract T accept<T>(Visitor<T> visitor);'

    res += '\n}'
    with open(f'src/com/esjr/bluejay/{base_name}.java', 'w') as f:
        f.write(res)

define_ast("Expr", [
    "Assign   : Token name, Expr value",
    "Binary   : Expr left, Token operator_, Expr right",
    "Call     : Expr callee, Token paren, List<Expr> arguments",
    "Grouping : Expr expression",
    "Literal  : object value",
    "Logical  : Expr left, Token operator_, Expr right",
    "Unary    : Token operator_, Expr right",
    "Variable : Token name"
])

define_ast("Stmt", [
    "Block      : List<Stmt> statements",
    "Break      : Token token, Expr value",
    "Expression : Expr expression",
    "Function   : Token name, List<Token> parameters, List<Stmt> body",
    "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
    "Print      : Expr expression",
    "Return     : Token keyword, Expr value",
    "Var        : Token name, Expr initializer",
    "While      : Expr condition, Stmt body"
])