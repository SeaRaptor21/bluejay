################################
##  WARNING                   ##
##  This file is depricated,  ##
##  please do NOT run it.     ##
##  I've made changes to the  ##
##  Values.java file by hand, ##
##  and running this would    ##
##  clear them.               ##
################################

def define_type(class_name, field_list, return_val):
    fields = field_list.split(', ')
    params = field_defs = ''
    if fields[0]:
        params = '\n            '.join([f'this.{f} = {f};' for f in [f.split(' ')[1] for f in fields]])
        field_defs = '\n        '.join([f'public final {f};' for f in fields])
    return '\n    '+f'''

    static class {class_name} extends Value {{
        {field_defs}
        {class_name}({field_list}) {{
            {params}
        }}
        public java.lang.String toString() {{
            return {return_val};
        }}
    }}

    '''.strip()+'\n'

def define_values(types):
    res = 'package com.esjr.bluejay;\n\nimport java.util.*;\n\n'
    res += 'abstract class Value {\n    public abstract java.lang.String toString();'

    for type in types:
        class_name = type.split('|')[0].strip()
        fields = type.split('|')[1].strip()
        return_val = type.split('|')[2].strip()
        res += define_type(class_name, fields, return_val)

    res += '}'
    with open('src/com/esjr/bluejay/Value.java', 'w') as f:
        f.write(res)

values = [
    "Number  | double value | java.lang.String.valueOf(value)",
    "String  | java.lang.String value | value",
    "Boolean | java.lang.Boolean value | value ? \"true\" : \"false\"",
    "List    | java.util.List<Value> elements | \"[\" + java.lang.String.join(\", \", elements.stream().map(e -> e.toString()).toArray()) + \"]\"", 
    "Dict    | Map<Value,Value> elements | \"{\" + java.lang.String.join(\", \", elements.keySet().stream().map(e -> e.toString()+\": \"+elements.get(e).toString()).toArray()) + \"}\"",
    "Null    | | \"null\""
]

# Commented out to prevent from running.
# define_values(values)