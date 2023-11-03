import React from "react";

import SmAnimatedCta from "../../../../components/buttons/SmAnimatedCta";
import { PAGE_ROUTES } from "../../../../../../constants/navigation";

import styles from "./addressSection.module.scss";

const AddressSection = ({
  id,
  title,
  subtitle,
  description,
  ctaText,
  image,
}) => (
  <section
    tabIndex="-1"
    id={id}
    className={`container-fluid ${styles.section} ${styles.addressSection}`}
    aria-labelledby="address-section-title"
  >
    <div
      className={`row flex-column-reverse flex-md-row ${styles.sectionContent}`}
    >
      <div className={`col-md-6 ${styles.addrContent}`}>
        <h2 className={styles.sectionMainTitle} id="address-section-title">
          {title}
        </h2>
        <h3 className={styles.sectionSubtitle}>{subtitle}</h3>
        <p className={styles.sectionDescription}>{description}</p>
        <SmAnimatedCta
          className={styles.sectionMainCta}
          href={PAGE_ROUTES.ADDRESS_REGISTRATION}
          isLink
        >
          {ctaText}
        </SmAnimatedCta>
      </div>
      <div className={`col-md-6 ${styles.addrMap}`}>
        <img src={image} className={styles.addrImg} alt="" />
      </div>
    </div>
  </section>
);

export default AddressSection;
