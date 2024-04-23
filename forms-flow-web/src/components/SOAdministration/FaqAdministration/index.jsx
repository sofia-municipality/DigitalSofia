import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Nav, Button, Row, Col } from "react-bootstrap";
import StarBorderIcon from "@mui/icons-material/StarBorder";
import StarIcon from "@mui/icons-material/Star";

import NavLink from "../../sm/components/Navigation/NavLink";
import Pagination from "../../sm/components/Pagination";
import AdministrationContainer from "../../AdministrationContainer";
import { fetchFAQ } from "../../../apiManager/services/faqServices";
import { PAGE_ROUTES } from "../../../constants/navigation";
import { useUpdateFAQ } from "../../../apiManager/apiHooks";

import styles from "./faqAdministration.module.scss";

const perPage = 10;

const FAQAdministration = () => {
  const { t } = useTranslation();
  const updateFAQ = useUpdateFAQ();
  const [page, setPage] = useState(1);
  const [data, setData] = useState({});
  const { faqs: faqList = [], total: totalItems } = data;

  useEffect(() => {
    const fetchData = async () => {
      const res = await fetchFAQ(page, perPage);
      setData(res.data);
    };

    fetchData();
  }, [page]);

  const onCtaClick = async (faq) => {
    const updatedFaq = Object.assign({}, faq, {
      isFavoured: !faq.isFavoured,
    });

    const payload = {
      title: updatedFaq.title,
      content: updatedFaq.content,
      isFavoured: updatedFaq.isFavoured,
    };

    const res = await updateFAQ(faq.id, payload, null, false);
    if (res) {
      setData((state) => {
        const faqs = [...state.faqs];
        const element = faqs.find((f) => f.id === faq.id);
        const index = faqs.indexOf(element);
        faqs.splice(index, 1, updatedFaq);

        return { ...state, faqs };
      });
    }
  };

  return (
    <AdministrationContainer title={t("so.administration.faqAdministration")}>
      <Row>
        <Col>
          <Nav.Link
            as={NavLink}
            to={PAGE_ROUTES.FAQ_ADD}
            className={`btn btn-success ${styles.addCta}`}
          >
            <span>{t("Add")}</span>
          </Nav.Link>
        </Col>
      </Row>
      {faqList.length ? (
        <div className={styles.content}>
          <div className={styles.blocksWrapper}>
            {faqList.map((faq, index) => (
              <div key={index} className={styles.faqWrapper}>
                <Button className={styles.icon} onClick={() => onCtaClick(faq)}>
                  {faq.isFavoured ? <StarIcon /> : <StarBorderIcon />}
                </Button>
                <Nav.Link
                  as={NavLink}
                  to={PAGE_ROUTES.FAQ_EDIT.replace(":faqId", faq.id).replace(
                    ":block",
                    faq?.id
                  )}
                  className={styles.faq}
                >
                  <span>{faq.title}</span>
                </Nav.Link>
              </div>
            ))}
          </div>
          <Pagination
            limit={perPage}
            totalItems={totalItems}
            selectedPage={page}
            changePage={(val) => setPage(val)}
          />
        </div>
      ) : null}
    </AdministrationContainer>
  );
};

export default FAQAdministration;
