/*******************************************************************************
 * Copyright (c) 2022 Digitall Nature Bulgaria
 *
 * This program and the accompanying materials
 * are made available under the terms of the Apache License 2.0
 * which accompanies this distribution, and is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
(function() {
    function setError(elementId, errorElementId, validity) {
        const element = document.getElementById(elementId);
        if(element) {
            let errorMsg;
            if(validity.valueMissing) {
                errorMsg = element.getAttribute('data-error-required');
            } else {
                errorMsg = element.getAttribute('data-error-invalid');
            }
            
            const errorEl = document.getElementById(errorElementId);
            if(errorEl) {
                errorEl.innerHTML = errorMsg;
                element.setAttribute('aria-invalid', true);
            }

            const submitCta = document.getElementById("submit-cta");
            if(submitCta) {
                submitCta.setAttribute('disabled', true);
            }
        }
    }

    function removeError(element, errorElementId) {
        const errorEl = document.getElementById(errorElementId);
        if(errorEl) {
            errorEl.innerHTML = '';
            element.setAttribute('aria-invalid', false);
        }
    }

    window.addEventListener('load', (event) => {
        const emailElement = document.getElementById('email');
        const phoneElement = document.getElementById('phone');
        const firstNameElement = document.getElementById('firstName');
        const lastNameElement = document.getElementById('lastName');
        const submitCta = document.getElementById("submit-cta");
        if(submitCta) {
            submitCta.setAttribute('disabled', true);
        }
        

        if(emailElement) {
            emailElement
            .addEventListener("keyup", function(e) {
                document.querySelector("#username").value = e.target.value;
            });

            emailElement.addEventListener("input", function(e) {
                const value = e.target.value;
                document.querySelector("#username").value = value;

                if(!emailElement.validity.valid) {
                    setError('email', 'input-error-email', emailElement.validity);
                } else {
                    removeError(emailElement, 'input-error-email')
                }

                if(emailElement.validity.valid && phoneElement.validity.valid && firstNameElement.validity.valid && lastName.validity.valid) {
                    submitCta.removeAttribute('disabled');
                }
            });

            emailElement.addEventListener('invalid', function(e) {
                e.preventDefault();
                setError('email', 'input-error-email', e.target.value);
            })
        }

        if(phoneElement) {
            phoneElement.addEventListener("input", function(e) {
                if(!phoneElement.validity.valid) {
                    setError('phone', 'input-error-phone', phoneElement.validity);
                } else {
                    removeError(phoneElement, 'input-error-phone')
                }

                if(emailElement.validity.valid && phoneElement.validity.valid && firstNameElement.validity.valid && lastName.validity.valid) {
                    submitCta.removeAttribute('disabled');
                }
            });

            phoneElement.addEventListener('invalid', function(e) {
                e.preventDefault();
                setError('phone', 'input-error-phone', e.target.value);
            })
        }

        if(firstNameElement) {
            firstNameElement.addEventListener("input", function(e) {
                if(!firstNameElement.validity.valid) {
                    setError('firstName', 'input-error-firstname', firstNameElement.validity);
                } else {
                    removeError(firstNameElement, 'input-error-firstname')
                }

                if(emailElement.validity.valid && phoneElement.validity.valid && firstNameElement.validity.valid && lastName.validity.valid) {
                    submitCta.removeAttribute('disabled');
                }
            });

            firstNameElement.addEventListener('invalid', function(e) {
                e.preventDefault();
                setError('firstName', 'input-error-firstname', e.target.value);
            })
        }

        if(lastNameElement) {
            lastNameElement.addEventListener("input", function(e) {
                if(!lastNameElement.validity.valid) {
                    setError('lastName', 'input-error-lastname', lastNameElement.validity);
                } else {
                    removeError(lastNameElement, 'input-error-lastname')
                }

                if(emailElement.validity.valid && phoneElement.validity.valid && firstNameElement.validity.valid && lastName.validity.valid) {
                    submitCta.removeAttribute('disabled');
                }
            });

            lastNameElement.addEventListener('invalid', function(e) {
                e.preventDefault();
                setError('lastName', 'input-error-lastname', e.target.value);
            })
        }
    });
}())