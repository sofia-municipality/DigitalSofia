import React, { useState, useCallback } from "react";
import { useParams } from "react-router-dom";
import AdministrationContainer from "../../../AdministrationContainer";
import { useTranslation } from "react-i18next";
import { Form, Button, Row, Col } from "react-bootstrap";

import { useGetBlock, useUpdateBlock } from "../../../../apiManager/apiHooks";
import { LANGUAGE } from "../../../../constants/constants";
import {
  PAGE_BLOCK_ATTRIBUTES,
  PAGE_BLOCKS,
} from "../../../../constants/pages";
import { getInputByType } from "../../utils";
import styles from "./blockEdit.module.scss";
import Loading from "../../../../containers/Loading";
import LanguageTabs from "../../LanguageTabs";

const BlockEdit = ({ page, block, isLoading, language, onSubmit }) => {
  const { t } = useTranslation();
  const blockId = block?.id;
  const blockMachineName = block?.["machine_name"];
  const blockAttributesConfig = PAGE_BLOCK_ATTRIBUTES[page][blockMachineName];

  const updateBlock = useUpdateBlock();

  const onFormSubmit = (e) => {
    e.preventDefault();
    const formData = new FormData(e.target);
    const formDataObj = Object.fromEntries(formData.entries());
    const rawPayload = Object.entries(formDataObj);

    const rawAttributes = rawPayload.reduce((payloadObject, [key, value]) => {
      let mappedValue;
      if (value instanceof File) {
        mappedValue = value.base64Url;
      } else {
        mappedValue = value;
      }

      if (key.includes("_")) {
        const payloadObjectArgs = key.split("_");
        const payloadObjectKey = payloadObjectArgs[0];
        const payloadObjectIndex = payloadObjectArgs[1];
        if (!payloadObject[payloadObjectKey]) {
          payloadObject[payloadObjectKey] = [];
        }

        if (payloadObjectIndex.includes(".")) {
          const innerKeyArgs = payloadObjectIndex.split(".");
          const innerKey = innerKeyArgs[1];
          const innerKeyIndex = innerKeyArgs[0];
          const currentObj = payloadObject[payloadObjectKey][innerKeyIndex];
          const obj = Object.assign({}, currentObj || {}, {
            [innerKey]: mappedValue,
          });

          payloadObject[payloadObjectKey][innerKeyIndex] = obj;
        } else {
          payloadObject[payloadObjectKey].push(mappedValue);
        }
      } else {
        payloadObject[key] = mappedValue;
      }

      return payloadObject;
    }, {});

    const attributes = Object.entries(rawAttributes).reduce(
      (payloadObject, [key, value]) => {
        if (Array.isArray(value)) {
          payloadObject[key] = value.filter((e) => e);
        } else {
          payloadObject[key] = value;
        }

        return payloadObject;
      },
      {}
    );

    updateBlock(
      blockId,
      {
        id: blockId,
        machine_name: blockMachineName,
        page,
        attributes,
      },
      language
    ).then(() => onSubmit());
  };

  return !isLoading ? (
    <Form className={styles.form} onSubmit={onFormSubmit}>
      {blockAttributesConfig?.map((fieldConfig, index) => (
        <Form.Group key={index} controlId={fieldConfig.fieldId}>
          {fieldConfig.label ? (
            <Form.Label>{t(fieldConfig.label)}</Form.Label>
          ) : null}
          {getInputByType(
            fieldConfig,
            block?.attributes?.[fieldConfig.fieldId]
          )}
          <Form.Text />
        </Form.Group>
      ))}
      <Row>
        <Col className={styles.submitCtaWrapper}>
          <Button variant="primary" type="submit">
            {t("form.submitCta")}
          </Button>
        </Col>
      </Row>
    </Form>
  ) : null;
};

const BlockEditWrapper = () => {
  const { t } = useTranslation();
  const [language, setLanguage] = useState(LANGUAGE);

  const { page, block: blockId } = useParams();
  const {
    data: block,
    isLoading,
    fetch: getBlock,
  } = useGetBlock(blockId, language);

  const blockMachineName = block?.["machine_name"];

  const blocksConfig = PAGE_BLOCKS[page];
  const title = Object.keys(blocksConfig).find(
    (key) => blocksConfig[key] === blockMachineName
  );

  const onChange = useCallback((lang) => setLanguage(lang), []);

  return title ? (
    <AdministrationContainer title={t(title)}>
      <LanguageTabs onChange={onChange} />
      <BlockEdit
        page={page}
        block={block}
        isLoading={isLoading}
        language={language}
        onSubmit={getBlock}
      />
    </AdministrationContainer>
  ) : (
    <Loading />
  );
};

export default BlockEditWrapper;
