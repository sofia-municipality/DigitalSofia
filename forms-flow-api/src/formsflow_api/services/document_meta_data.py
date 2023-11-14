from xml.etree import ElementTree
import re

class DocumentMetaData:
    def __init__(self, source):
        self.__element_tree = ElementTree.parse(source)
        self.__regex_should_modify = r"#{([\/\w*]+)( \?\? (.+))?}"

    def parse_with_dict(self, data: dict):
        root = self.__element_tree.getroot()
        self.__iterate_recurs(root, data=data)

    def print(self):
        root = self.__element_tree.getroot()
        self.__print_recurs(root)

    def convert_to_string(self) -> str:
        str = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n'
        str += ElementTree.tostring(self.__element_tree.getroot(), encoding="unicode")

        return str

    def convert_to_dict(self):
        root = self.__element_tree.getroot()
        return self.__convert_to_dict_recurs(root)

    def __convert_to_dict_recurs(
        self,
        element: ElementTree.Element,
    ):
        to_return = {}

        has_children = bool(list(element))
        if has_children:
            
            ### Is this a dict or a list return type
            should_return_be_dict = True
            child_tag_list = [] 
            for child in element:
                if child.tag in child_tag_list:
                    should_return_be_dict = False
                    break

                child_tag_list.append(child.tag)

            if should_return_be_dict:
                to_return = {}
                for child in element[:]:
                    to_return[child.tag] = self.__convert_to_dict_recurs(child)
                return to_return
            
            to_return = []
            for child in element:
                to_return.append(
                    { child.tag: self.__convert_to_dict_recurs(child) }
                )

            return to_return

        else:
            return element.text



    def __print_recurs(
        self, 
        element: ElementTree.Element, 
        depth: int = 0
    ):
        has_children = bool(list(element))

        # if the element has no children, we are at a final node so we should try and replace the text there
        if not has_children:
            print("%s<%s>%s</%s>" % ("    " * depth,element.tag,element.text,element.tag))
        else:
            # print("%s %s - NoText - Children - %s" % ("    " * depth, element.tag, has_children))
            ### Go through children
            ### If no replacement was made, remove the child
            print("%s<%s>" % ("    " * depth, element.tag))
            for child in element:
                self.__print_recurs(child, depth=depth+1)
                # if not was_replacement_made:
                #     element.remove(child)
            print("%s</%s>" % ("    " * depth, element.tag))

    ### Go through nodes
    ###     if it is a final node:
    ###         - is it a replacement node, if it is a replacement node try and replace it.
    ###              - if a replacement is made, we shouldn't delete it
    ###              - if a replacement is not made, delete it 
    ###         - is it a string node, don't touch it
    ###     if it is a parent node, go through nodes:
    ###         - if the child node is a parent node, and is empty delete it
    ###         - if the child node is a replacement node, and no replacement was made, delete it
    ###         - If the child node is a text node, leave it
    ###     if only text nodes are left, delete this one, check if any nodes are left there
    ### This method returns Should we delete, and the type of node
    def __iterate_recurs(
        self, 
        element: ElementTree.Element, 
        depth: int = 0, 
        data = {}
    ) -> bool:
        ### Determine type of node
        ### Is parent node
        shouldDelete = False
        if bool(list(element)):
            ### Go through children
            text_nodes = []
            # print("%s<%s>" % ("    " * depth, element.tag))
            # print("%s Elements = %s" % ("    " * depth, len(element)))
            for child in element[:]:
                shouldDelete, nodeType = self.__iterate_recurs(element=child, depth=depth+1, data=data)

                if nodeType in ["ReplacementNode", "ParentNode"] and shouldDelete:
                    element.remove(child)
                else:
                    text_nodes.append(element)

            ### Are there any nodes left?
            # print("%s Elements = %s" % ("    " * depth, len(element)))
            if len(element) == 0:
                # print("%s</%s> - %s" % ("    " * depth, element.tag, True))
                return True, "ParentNode"
            
            ### Check if only text nodes are left


            # shouldDelete = len(text_nodes) == len(element)
            #  
            shouldDelete = False
            # print("%s</%s> - %s" % ("    " * depth, element.tag, shouldDelete))
                
            return shouldDelete, "ParentNode"        
        else:
            ### We're at final node
            matches = self.__should_modify_element_matches(element=element)
            
            if not matches:
                ### This is a simple text node
                # print("%s<%s>%s</%s> - TextNode - False" % ("    " * depth,element.tag,element.text,element.tag))
                return False, "TextNode"

            ### Try and get replacement values
            replacement_value = self.__get_replacement_from_data(matches=matches, data=data)

            ### If no replacement value is found, delete the node
            shouldDelete = replacement_value == None 
            element.text = str(replacement_value) if not shouldDelete else None
            # print("%s<%s>%s</%s> - ReplacementNode - %s" % ("    " * depth,element.tag,element.text,element.tag, shouldDelete))
            return shouldDelete, "ReplacementNode"

    def __get_replacement_from_data(self, matches, data: dict):
        data_path = matches[1]
        has_default = bool(matches[2])
        default_value = matches[3] if has_default else None

        ### Remove leading / 
        data_path = data_path.lstrip("/")
        ### Get keys
        keys = data_path.split("/")
        leading_keys = keys[:-1]
        last_key = keys[-1]

        ### Go to the last dict
        invalid_path = False
        latest_data = data
        for key in leading_keys:
            if key not in latest_data or type(latest_data[key]) is not dict:
                invalid_path = True
                break

            latest_data = data[key]

        ### If no default value is set, the default value is presumed to be None
        ### If we couldn't find a valid path, return default value
        ### Else try and get the value by the last key from the dict
        return default_value if invalid_path else latest_data.get(last_key, default_value)


    def __should_modify_element_matches(self, element: ElementTree.Element) -> re.Match:
        if not element.text:
            print(f"{element.tag} - error in")
            return False
        text = element.text.strip()
        return re.match(self.__regex_should_modify, text)
