from typing import List
from math import fsum
from datetime import datetime
from flask import current_app
from formsflow_api.services.external import EFormIntegrationsService
from formsflow_api.models.valid_debt_reg_id import VALID_DEBT_REG_ID
from formsflow_api.exceptions.common import CommonException


class PaymentValidationService:
    """ Service class for payment validation. """
    __payment_request_obligations: List
    __mateus_obligations: List

    def __init__(self, payment_request_obligations: list, person_identifier: str):
        self.__payment_request_obligations = payment_request_obligations
        self.__mateus_obligations = self.fetch_mateus_obligations(person_identifier)

    def get_mateus_obligations(self):
        return self.__mateus_obligations

    def get_payment_request_obligations(self):
        return self.__payment_request_obligations

    def validate_incoming_obligations(self):
        current_year_obligations = self.filter_current_year_obligations()

        # Check if request payment obligations are for the current year only
        if current_year_obligations and len(current_year_obligations) == len(self.__payment_request_obligations):
            # Validation of the obligations with the mateus obligations for the current year only
            self.validate_current_year_obligations()

            # TODO: for test purposes
            # raise Exception(f"Problem with the obligations for the current year only.")
        else:
            # Validation of the obligations with the mateus obligations
            # by group when the obligations are not for the current year only
            incoming_obligations_groups = self.sort_obligations_by_group()
            mateus_obligations_groups = self.sort_mateus_obligations_by_group()

            current_app.logger.debug(incoming_obligations_groups)
            current_app.logger.debug(mateus_obligations_groups)

            # Map the obligations
            self.map_and_validate_groups(incoming_obligations_groups, mateus_obligations_groups)

    def filter_current_year_obligations(self):
        current_year = datetime.now().year
        current_app.logger.debug(
            f"Filtering obligations for the year: {current_year}")
        return [
            obligation for obligation in self.__payment_request_obligations
            if obligation["taxPeriodYear"] == str(current_year)
        ]

    def validate_current_year_obligations(self):
        # Map the obligations by partidaNo and then sort for each of them by payOrder
        request_obligations = {}
        for obligation in self.__payment_request_obligations:
            partida_no = obligation["partidaNo"]
            kind_debt_reg_id = obligation["kindDebtRegId"]

            # Create a unique index for each obligation
            request_obligation_index = f"{partida_no}-{kind_debt_reg_id}"

            # Check if the index exists in the request_obligations
            if request_obligation_index not in request_obligations:
                request_obligations[request_obligation_index] = []
            # Append the obligation to the list of obligations for the index
            request_obligations[request_obligation_index].append(obligation)

        # Sort the obligations for each of the indexes by payOrder
        for request_obligation_index in request_obligations:
            request_obligations[request_obligation_index].sort(key=lambda x: x["payOrder"])

        # Validate with mateus obligations for each of the obligations
        mateus_obligations = self.group_mateus_obligations_by_partida_and_kind_debt_reg_id()

        # Validate the obligations
        for index, obligations in request_obligations.items():
            current_app.logger.info(f"Validating obligations for index: {index}")
            mateus_obligations_by_index = mateus_obligations.get(index, {})

            for obligation in obligations:
                # Check if the obligation exists in the mateus_obligations
                matching_mateus_obligation = next(
                    (mo for mo in mateus_obligations_by_index.values() if
                     mo["debtInstalmentId"] == obligation["debtInstalmentId"]),
                    None
                )
                if not matching_mateus_obligation:
                    raise CommonException(
                        message=f"Missing obligation with debtInstalmentId {obligation['debtInstalmentId']} "
                                f"in the external system obligations for partidaNo {partida_no}.",
                        data={
                            "debtInstalmentId": obligation["debtInstalmentId"],
                            "partidaNo": partida_no
                        },
                        key="missing_obligation_in_mateus"
                    )

                # checking whether an obligation installment has not been skipped
                skipped_obligation_check = next(
                    (mateus_obligations_by_index[inst_no] for inst_no in mateus_obligations_by_index.keys() if
                     int(inst_no) < obligation["instNo"] and mateus_obligations_by_index[inst_no] not in obligations),
                    None
                )
                if skipped_obligation_check:
                    raise CommonException(
                        message=f"Obligation with debtInstalmentId {skipped_obligation_check['debtInstalmentId']} and instNo "
                                f"{skipped_obligation_check['instNo']} has been skipped in the incoming "
                                f"obligations for payment with partidaNo {partida_no}.",
                        key="skipped_obligation_in_incoming_obligations"
                    )

                # Compare the obligations for the current year
                success, field = self.compare_obligations(obligation, matching_mateus_obligation)
                if not success:
                    raise CommonException(
                        message=f"Field {field} for obligation {obligation['debtInstalmentId']} does not match "
                                f"with the existing data in the external system obligations for partidaNo {partida_no}.",
                        key="obligation_field_mismatch"
                    )

    def group_mateus_obligations_by_partida_and_kind_debt_reg_id(self):
        mateus_obligations = {}

        current_app.logger.info(f"Grouping mateus obligations by partidaNo and kindDebtRegId")

        for obligation in self.__mateus_obligations:
            partida_no = obligation["partidaNo"]
            kind_debt_reg_id = obligation["kindDebtRegId"]

            # Create a unique index for each obligation
            index = f"{partida_no}-{kind_debt_reg_id}"
            if index not in mateus_obligations:
                mateus_obligations[index] = {}
            mateus_obligations[index][f"{obligation['instNo']}"] = obligation
        return mateus_obligations

    def map_and_validate_groups(self, incoming_obligations_groups, mateus_obligations_groups):
        for group_name, incoming_obligations in incoming_obligations_groups.items():
            mateus_group_data = mateus_obligations_groups.get(group_name, {})

            if not mateus_group_data:
                current_app.logger.info(f"No obligations for {group_name} type in Mateus")
                continue

            for index, incoming_obligation in enumerate(incoming_obligations):
                if index < len(mateus_group_data):
                    priority, mateus_obligation = mateus_group_data[index]

                    incoming_obligation = incoming_obligations.get(str(mateus_obligation["debtInstalmentId"]), None)
                    # Check if the obligation exists in the mateus_obligations
                    if incoming_obligation is None:
                        raise CommonException(
                            message=f"Missing obligation with debtInstalmentId {mateus_obligation['debtInstalmentId']} "
                                    f"in the incoming obligations for payment.",
                            key="skipped_obligation_in_incoming_obligations"
                        )

                    success, field = self.compare_obligations(incoming_obligation, mateus_obligation)

                    if not success:
                        raise CommonException(
                            message=f"Field {field} for obligation {mateus_obligation['debtInstalmentId']} "
                                    f"does not match with the existing data.",
                            key="obligation_field_mismatch"
                        )
                else:
                    # Handle the case where there are more incoming obligations than in mateus_group_data
                    break

            if len(mateus_group_data) > len(incoming_obligations):

                current_app.logger.info(f"{len(incoming_obligations) - 1} - {len(incoming_obligations)}")
                # Check for same payOrder that`s left behind
                mateus_last_paid_obligation = mateus_group_data[len(incoming_obligations) - 1]
                mateus_next_unpaid_obligation = mateus_group_data[len(incoming_obligations)]

                current_app.logger.info(f"mateus_last_paid_obligation: {mateus_last_paid_obligation}")
                current_app.logger.info(f"mateus_next_unpaid_obligation: {mateus_next_unpaid_obligation}")

                # compare the payOrder of the obligations
                if mateus_last_paid_obligation[0] == mateus_next_unpaid_obligation[0]:
                    raise CommonException(
                        message=f"Someone missed to pay an obligation with payOrder {mateus_last_paid_obligation[0]}.",
                        key="missed_obligation_payment"
                    )

        return {
            "success": True,
            "error": None
        },

    @staticmethod
    def compare_obligations(incoming_obligation, mateus_obligation):

        if not incoming_obligation or not mateus_obligation: return False, ""

        for field in incoming_obligation:
            # current_app.logger.info(f"{incoming_obligation.get(field)}, {mateus_obligation.get(field)}")
            if incoming_obligation.get(field) != mateus_obligation.get(field): return False, field

        return True, ""

    def sort_obligations_by_group(self):
        groups = {
            "real_estate": {},
            "vehicle": {},
            "household_waste": {},
        }

        for obligation in self.__payment_request_obligations:
            # Check if the debt type is valid
            valid_debt_type = VALID_DEBT_REG_ID.get(obligation["kindDebtRegId"], None)

            # Calculate the cost of the obligation
            obligation_cost = round(fsum([obligation["interest"], obligation["residual"]]), 2)

            # Check if it is a debt type we must handle
            if valid_debt_type and obligation_cost > 0:
                groups[valid_debt_type][str(obligation["debtInstalmentId"])] = obligation

        # Remove groups that do not contain any obligations
        groups = {debt_type: obligations for debt_type, obligations in groups.items() if obligations}

        current_app.logger.debug(f"Sorted obligations by group: {groups}")

        return groups

    @staticmethod
    def fetch_mateus_obligations(person_identifier: str):
        client = EFormIntegrationsService()
        response = client.get_obligations(person_identifier)

        obligations = response.get('obligations', [])

        if not obligations:
            raise CommonException(
                message="No obligations found in the external system.",
                key="no_obligations_in_mateus"
            )

        return obligations

    def sort_mateus_obligations_by_group(self):
        groups = {
            "real_estate": [],
            "vehicle": [],
            "household_waste": [],
        }

        for obligation in self.__mateus_obligations:
            # Check if the debt type is valid
            valid_debt_type = VALID_DEBT_REG_ID.get(obligation["kindDebtRegId"], None)

            # Calculate the cost of the obligation
            obligation_cost = round(fsum([obligation["interest"], obligation["residual"]]), 2)

            # Check if it is a debt type we must handle
            if valid_debt_type and obligation_cost > 0:
                groups[valid_debt_type].append([obligation["payOrder"], obligation])

        # Sort each group by payOrder and keep the payOrder in the final structure
        for debt_type, obligations_list in groups.items():
            sorted_obligations = sorted(obligations_list, key=lambda x: x[0])  # Sort by payOrder
            groups[debt_type] = sorted_obligations  # Replace with the sorted list, keeping payOrder

        return groups

# SAMPLE USAGE:
# if __name__ == "__main__":
#     payment_validation_service = PaymentValidationService(json_data, "9109120943")
#
#     payment_validation_service.validate_incoming_obligations()
