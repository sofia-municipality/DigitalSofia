const form_options = { readOnly: false, renderMode: "flat" };
// Function to get submission data if formadapter is enabled
async function fetchSubmission() {
  const submission = await fetch(form_info.submission_url, {
    headers: {
      "Content-Type": "application/json",
      Authorization: form_info.auth_token
      }
  });
  const result = await submission.json();
  return result;
}

// Help web driver to identify the form rendered completely.
function formReady() {
  document.getElementById("formio").classList.add("completed");
}

// Render form with form adapter 
function renderFormWithSubmission() {
      Formio.createForm(
        document.getElementById("formio"),
        form_info.form_url,
        form_options
      ).then((form) => {
          var formattedSubmission = {
            metadata : {},
            data : form_info.data.data
          }
          formattedSubmission.data.is_pdf = true
          var translations = {}
          form_info.translations.forEach(trans => {
            translations[Object.keys(trans)[0]] = trans[Object.keys(trans)[0]]
          })
          form.addLanguage(form_info.language, translations);
          form.language = form_info.language
          form.submission = formattedSubmission
          form.ready.then(() => {
            setTimeout(() => {
                formReady();
            }, 1000)
          });
      });
}

// Render form from formio
function renderFormWithOutSubmission() {
  Formio.createForm(
    document.getElementById("formio"),
    form_info.form_url,
    form_options
  ).then((form) => {
    form.ready.then(() => {
      formReady();
    });
  });
}

function renderForm() {
  // loading custom components from formsflow-formio-custom-elements (npm package)
  try {
    const components = FormioCustom.components;
    for (var key of Object.keys(components)) {
      Formio.registerComponent(key, components[key]);
    }
  } catch (err) {
    console.log("Cannot load custom components.");
  }

  try {
    Formio.setBaseUrl(form_info.base_url);
    Formio.setProjectUrl(form_info.project_url);
    Formio.setToken(form_info.token);

    if (form_info.form_adapter) {
      renderFormWithSubmission();
    } else {
      renderFormWithOutSubmission();
    }
  } catch (err) {
    console.log("Cannot render form", err);
    document.getElementById("formio").innerHTML('Cannot render form')
    formReady();
  }


}
