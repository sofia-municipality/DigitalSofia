from mako.template import Template
import os
from formsflow_api.workers.notification_config import NotificationConfig

# In order to have syntax highlighting for the mako template in VSCode install this extension:
# https://marketplace.visualstudio.com/items?itemName=tommorris.mako


class RenderResult:
    """ Descibes the result of the rendering of the mail template 
        success: true if the rendering of the templare is successful, otherwise false
        output: The rendering output can be a string or binary string
                If success is false the output will contain the error message!
    """
    success:bool
    output:str
    def __init__(self, success:bool, output:str):
        self.success = success
        self.output = output

class MailMergeService:
    """ Mail rendering service using Mako engine """
    __config: NotificationConfig
    
    def __init__(self, config: NotificationConfig):
        self.__config = config
        #print(config.templates_location)

    def render(self, template_name:str, *args, **kwargs)->RenderResult:
        """ Renders the mail template based on the arguments
            Usually the mail tempaltes have some internal validaton of the required parameters 
            and if they are not provided the result of the rendering will be an empty string (almost empty).
            If you implement new mail template ensure that nothing is rendered (empty string), 
            if some of the required for the template parameters are missing. 
            This will ensure that the rendered output is not malformated or invalid. 
        """
        try:
            if not template_name: raise Exception("template_name is required")

            template_path = os.path.abspath(os.path.join(self.__config.templates_location, template_name))

            template = Template(filename = template_path, 
                             input_encoding='utf-8', 
                             output_encoding='utf-8')
        
            output = template.render(*args, **kwargs)

            if len(output) > 5: #There can be some parasite intervals or new lines so we ignore them
                return RenderResult(True, output)
            else:
                return RenderResult(False, f"Render of '{template_name}' failed.")
        
        except KeyError as err:
            return RenderResult(False, f"Required {err} parameter missing to render the '{template_name}'.")
        except Exception as err:
            return RenderResult(False, f"Error '{err}' rendering {template_name}.")
        

