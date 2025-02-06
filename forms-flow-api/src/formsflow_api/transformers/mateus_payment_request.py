from flask import current_app
from math import fsum


class MateusPaymentRequestTransformer:
    def __init__(self):
        current_app.logger.debug("MateusPaymentRequestTransformer Init")

    @staticmethod
    def transform(group_id, obligation):
        if obligation["propertyAddress"] != " ":
            additional_data = obligation["propertyAddress"]
        else:
            additional_data = obligation["registerNo"]

        payment_dict = {
            "reason": str(obligation["taxPeriodYear"]) + "-" + str(obligation["instNo"]) + "-" + str(
                obligation["partidaNo"]),
            "group_id": group_id,
            "amount": float(round(fsum([obligation["residual"], obligation["interest"]]), 2)),
            "tax_period_year": int(obligation["taxPeriodYear"]),
            "partida_no": obligation["partidaNo"],
            "kind_debt_reg_id": int(obligation["kindDebtRegId"]),
            "pay_order": int(obligation["payOrder"]),
            "additional_data": additional_data,
            "rnu": obligation["rnu"],
            "municipality_id": obligation["municipalityId"],
            "residual": obligation["residual"],
            "interest": obligation["interest"],
            "debt_instalment_id": obligation["debtInstalmentId"],
        }

        return payment_dict
