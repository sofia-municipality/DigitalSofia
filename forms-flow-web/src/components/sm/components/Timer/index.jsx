import React from "react";

import styles from "./timer.module.scss";

const padWithZeros = (timeUnit) =>
  timeUnit < 10 ? `0${timeUnit}` : `${timeUnit}`;

const Timer = ({ minutes, seconds, hours }) => (
  <div className={styles.timer}>
    {hours ? (
      <>
        <span>{padWithZeros(hours)}</span>
        <span>{":"}</span>
      </>
    ) : null}
    <span>{padWithZeros(minutes)}</span>
    <span>{":"}</span>
    <span>{padWithZeros(seconds)}</span>
  </div>
);

export default Timer;
