import React from "react";

const TaskSort = React.memo(({ handleClick, options }) => {
  const onSortSelect = (sortType) => {
    handleClick(sortType);
  };

  return (
    <div className="sort-items">
      {options.map((sortType, index) => (
        // eslint-disable-next-line jsx-a11y/click-events-have-key-events, jsx-a11y/no-static-element-interactions
        <div
          key={index}
          className="clickable p-0 mb-2"
          onClick={() => onSortSelect(sortType)}
        >
          {sortType.label}
        </div>
      ))}
    </div>
  );
});

export default TaskSort;
